package com.fabiocondo.repository;

import com.fabiocondo.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);
    Wallet findByUserIdAndIsDefaultTrue(Long userId);
    boolean existsByUserIdAndPhoneNumber(Long userId, String phoneNumber);
}

