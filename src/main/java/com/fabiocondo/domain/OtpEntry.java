package com.fabiocondo.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_entries")
public class OtpEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiry;

    private String email;

    public OtpEntry() {
    }

    public OtpEntry(String code, LocalDateTime expiry, String email) {
        this.code = code;
        this.expiry = expiry;
        this.email = email;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public boolean isExpired() {
        return expiry.isBefore(LocalDateTime.now());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}



