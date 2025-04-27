package com.fabiocondo.service.impl;

import com.fabiocondo.domain.OtpEntry;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserPrincipal;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.OtpRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.security.service.EmailService;
import com.fabiocondo.security.utility.JWTTokenProvider;
import com.fabiocondo.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

import static com.fabiocondo.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.fabiocondo.enumeration.Role.ROLE_USER;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final int EXPIRATION_MINUTES = 15;
    private final EmailService emailService;
    private final JWTTokenProvider jwtTokenProvider;


    public OtpService(OtpRepository otpRepository, UserRepository userRepository, UserService userService, EmailService emailService, JWTTokenProvider jwtTokenProvider) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public String generateOtp(String email) throws UserNotFoundException, MessagingException {

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("Usuário não encontrado pelo email: " + email);
        }

        // Remove o OTP anterior se existir e força o commit da exclusão
        otpRepository.findByEmail(email).ifPresent(existingOtp -> {
            otpRepository.delete(existingOtp);
            otpRepository.flush(); // 👈 Garante que o delete seja executado imediatamente no banco
        });

        // Gera novo OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        // Salva novo OTP
        OtpEntry entry = new OtpEntry();
        entry.setEmail(email);
        entry.setCode(otp);
        entry.setExpiry(expiry);

        otpRepository.save(entry);

        // Envia email
        emailService.sendNewPasswordEmail(user.getFullName(), user.getEmail(), otp);

        return otp;
    }

    @Transactional
    public ResponseEntity<?> validateOtp(String email, String otp) throws OtpNotFoundException, UserNotFoundException, OtpExpiredException, InvalidOtpException {

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("Usuário não encontrado pelo email: " + email);
        }

        OtpEntry entry = otpRepository.findByEmail(email)
                .orElseThrow(() -> new OtpNotFoundException("Nenhum OTP encontrado para o usuário com email: " + user.getEmail()));

        if (entry.getExpiry().isBefore(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            throw new OtpExpiredException("O OTP expirou");
        }

        if (!entry.getCode().equals(otp)) {
            throw new InvalidOtpException("Código OTP inválido");
        }

        // OTP é válido, então remove do banco
        otpRepository.delete(entry);

        // Gera JWT e retorna
        UserPrincipal userPrincipal = new UserPrincipal(user);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);

        return new ResponseEntity<>(user, jwtHeader, HttpStatus.OK);
    }

    @Transactional
    public String startRegistration(String fullName, String email) throws MessagingException {

        // Verifica se já existe um usuário com esse email
        User existingUser = userRepository.findUserByEmail(email);
        if (existingUser != null) {
            throw new IllegalStateException("Já existe um usuário registrado com este email.");
        }

        // Remove OTP antigo (se existir)
        otpRepository.findByEmail(email).ifPresent(existingOtp -> {
            otpRepository.delete(existingOtp);
            otpRepository.flush();
        });

        // Gera novo OTP
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        // Salva o OTP no banco de dados sem associar a um usuário ainda
        OtpEntry entry = new OtpEntry();
        entry.setEmail(email); // Associa o OTP ao email do usuário
        entry.setCode(otp);
        entry.setExpiry(expiry);

        otpRepository.save(entry);

        // Envia email com o OTP
        emailService.sendNewPasswordEmail(fullName, email, otp);

        return otp;
    }

    @Transactional
    public ResponseEntity<?> completeRegistration(String fullName, String email, String otp)
            throws OtpNotFoundException, OtpExpiredException, InvalidOtpException, UserNotFoundException, EmailExistException, MessagingException {

        OtpEntry entry = otpRepository.findByEmail(email)
                .orElseThrow(() -> new OtpNotFoundException("Nenhum OTP encontrado para o email: " + email));

        if (entry.getExpiry().isBefore(LocalDateTime.now())) {
            otpRepository.delete(entry);
            throw new OtpExpiredException("O OTP expirou");
        }

        if (!entry.getCode().equals(otp)) {
            throw new InvalidOtpException("Código OTP inválido");
        }

        // OTP válido, deletar
        otpRepository.delete(entry);

        User user = userService.register(fullName, email, null);

        // Gera JWT para o novo usuário
        UserPrincipal userPrincipal = new UserPrincipal(user);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);

        return new ResponseEntity<>(user, jwtHeader, HttpStatus.CREATED);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }
}
