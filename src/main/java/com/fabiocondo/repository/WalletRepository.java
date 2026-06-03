package com.fabiocondo.repository;

import com.fabiocondo.domain.Wallet;
import com.fabiocondo.enumeration.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);
    Optional<Wallet> findByUserIdAndPhoneNumberAndType(Long userId, String phoneNumber, WalletType type);
    //Wallet findByUserIdAndIsDefaultTrue(Long userId);
    //boolean existsByUserIdAndPhoneNumber(Long userId, String phoneNumber);
}

