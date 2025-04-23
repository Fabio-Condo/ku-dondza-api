package com.fabiocondo.repository;

import com.fabiocondo.domain.OtpEntry;
import com.fabiocondo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntry, Long> {
    Optional<OtpEntry> findByUser(User user);
    void deleteByUser(User user);
}



