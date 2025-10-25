package com.fabiocondo.service.impl;

import com.fabiocondo.domain.User;
import com.fabiocondo.domain.Wallet;
import com.fabiocondo.exception.domain.UserNotFoundException;
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

    public List<Wallet> getWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public Wallet addWallet(Long userId, Wallet wallet) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        wallet.setUser(user);
        return walletRepository.save(wallet);
    }
}
