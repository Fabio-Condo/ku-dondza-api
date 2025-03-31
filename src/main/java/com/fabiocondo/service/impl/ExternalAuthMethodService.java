package com.fabiocondo.service.impl;

import com.fabiocondo.domain.ExternalAuthMethod;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.AuthProvider;
import com.fabiocondo.repository.ExternalAuthMethodRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class ExternalAuthMethodService {

    private final ExternalAuthMethodRepository externalAuthMethodRepository;
    private final UserRepository userRepository;

    public ExternalAuthMethodService(ExternalAuthMethodRepository externalAuthMethodRepository, UserRepository userRepository) {
        this.externalAuthMethodRepository = externalAuthMethodRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addExternalAuth(Long userId, AuthProvider provider, String providerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<ExternalAuthMethod> existingMethod = externalAuthMethodRepository
                .findByUserAndProvider(user, provider);

        if (existingMethod.isPresent()) {
            throw new RuntimeException("Método de autenticação já cadastrado");
        }

        ExternalAuthMethod newMethod = new ExternalAuthMethod(provider, providerId, user);

        externalAuthMethodRepository.save(newMethod);
    }

    @Transactional
    public void removeExternalAuth(Long userId, AuthProvider provider) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Set<ExternalAuthMethod> methods = user.getExternalAuthMethods();

        if (methods.size() == 1) {
            throw new RuntimeException("Não é possível remover o único método de autenticação");
        }

        externalAuthMethodRepository.deleteByUserAndProvider(user, provider);
    }
}

