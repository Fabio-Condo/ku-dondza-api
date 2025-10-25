package com.fabiocondo.controller;

import com.fabiocondo.domain.Wallet;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wallet>> getWalletsByUser(@PathVariable Long userId) {
        List<Wallet> wallets = walletService.getWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    @PostMapping("/user/{userId}/add")
    public ResponseEntity<Wallet> addWallet(@PathVariable Long userId, @RequestBody Wallet wallet) throws UserNotFoundException {
        Wallet savedWallet = walletService.addWallet(userId, wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWallet);
    }
}

