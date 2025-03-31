package com.fabiocondo.domain;

import com.fabiocondo.enumeration.AuthProvider;

import javax.persistence.*;

@Entity
@Table(name = "external_auth")
public class ExternalAuthMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId; // ID do Google, Facebook, etc. (opcional)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ExternalAuthMethod() {
    }

    public ExternalAuthMethod(AuthProvider provider, String providerId, User user) {
        this.provider = provider;
        this.providerId = providerId;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
