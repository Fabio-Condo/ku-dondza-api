package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.Wallet;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.exception.domain.WalletNotFoundException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public Wallet findById(Long id) throws WalletNotFoundException {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("No wallet found by id: " + id));
    }

    public List<Wallet> getWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public Wallet addWallet(Long userId, Wallet wallet) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        wallet.setUser(user);
        return walletRepository.save(wallet);
    }

    public Wallet setDefault(Long walletId) throws WalletNotFoundException {
        Wallet wallet = findById(walletId);

        User user = wallet.getUser();

        // Desmarca todas as outras carteiras do usuário
        user.getWallets().forEach(w -> {
            if (!w.getId().equals(walletId)) {
                w.setDefault(false);
            }
        });

        // Marca a carteira selecionada como default
        wallet.setDefault(true);

        // Salva alterações
        walletRepository.saveAll(user.getWallets()); // atualiza as outras
        return walletRepository.save(wallet); // atualiza a selecionada
    }

    public void deactivateWallet(Long walletId) throws WalletNotFoundException {
        Wallet wallet = findById(walletId);
        wallet.setActive(false);
        walletRepository.save(wallet);
    }
}
