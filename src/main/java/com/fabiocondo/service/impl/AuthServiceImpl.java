package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.AuthProvider;
import com.fabiocondo.repository.ExternalAuthMethodRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.security.utility.JWTTokenProvider;
import com.fabiocondo.service.AuthService;
import com.fabiocondo.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

import static com.fabiocondo.constant.SecurityConstant.JWT_TOKEN_HEADER;

@Service
public class AuthServiceImpl implements AuthService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserService userService;
    private final UserRepository userRepository;
    private final ExternalAuthMethodRepository externalAuthMethodRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;
    private static final String CLIENT_ID = "170476897572-k758vjru9e2qqa707qhb5ns2kaaegquc.apps.googleusercontent.com";


    @Autowired
    public AuthServiceImpl(UserService userService, UserRepository userRepository, ExternalAuthMethodRepository externalAuthMethodRepository, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.externalAuthMethodRepository = externalAuthMethodRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public ResponseEntity<User> authenticateWithUsernameAndPassword(User user) {
        authenticate(user.getEmail(), user.getPassword());
        User loginUser = userService.findUserByEmail(user.getEmail());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> authenticateWithGoogle(String idTokenString) {
        logger.info("Passando daqui...");
        try {
            GoogleIdToken idToken = verifyGoogleToken(idTokenString);
            if (idToken == null) {
                return ResponseEntity.badRequest().body("Token inválido ou expirado");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String providerId = payload.getSubject(); // ID único do Google
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // 🔹 1️⃣ Verificar se já existe um método de autenticação para este `providerId`
            Optional<ExternalAuthMethod> existingAuthMethod =
                    externalAuthMethodRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, providerId);

            User loginUser;
            if (existingAuthMethod.isPresent()) {
                // ✅ Usuário já logou antes com Google -> Buscar associado
                loginUser = existingAuthMethod.get().getUser();
            } else {
                // 🔹 2️⃣ Verificar se o e-mail já está cadastrado
                User existingUser = userRepository.findUserByEmail(email);

                if (existingUser != null) {
                    // ✅ Usuário já existe, associamos o login Google
                    loginUser = existingUser;
                } else {
                    // 🔹 3️⃣ Criar novo usuário e associar o método Google
                    loginUser = userService.register(name, email, pictureUrl);
                }

                // 🔹 4️⃣ Salvar método de autenticação Google para o usuário
                ExternalAuthMethod authMethod = new ExternalAuthMethod();
                authMethod.setUser(loginUser);
                authMethod.setProvider(AuthProvider.GOOGLE);
                authMethod.setProviderId(providerId);
                externalAuthMethodRepository.save(authMethod);
            }

            // 🔹 5️⃣ Gerar token JWT e retornar resposta
            UserPrincipal userPrincipal = new UserPrincipal(loginUser);
            HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
            return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Erro ao verificar o token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao verificar o token");
        }
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) throws GeneralSecurityException, IOException, JSONException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        String idToken = extractIdToken(idTokenString);
        return verifier.verify(idToken);
    }

    public String extractIdToken(String jsonString) throws JSONException {
        // Converte a string JSON em um objeto JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);

        // Extrai o valor do idToken
        return jsonObject.getString("idToken");
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}

