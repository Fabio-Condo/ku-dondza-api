package com.fabiocondo.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_entries")
public class OtpEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiry;

    public OtpEntry() {
    }

    public OtpEntry(User user, String code, LocalDateTime expiry) {
        this.user = user;
        this.code = code;
        this.expiry = expiry;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}



