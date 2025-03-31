package com.fabiocondo.repository;

import com.fabiocondo.domain.ExternalAuthMethod;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExternalAuthMethodRepository extends JpaRepository<ExternalAuthMethod, Long> {

    Optional<ExternalAuthMethod> findByUserAndProvider(User user, AuthProvider provider);

    void deleteByUserAndProvider(User user, AuthProvider provider);

    Optional<ExternalAuthMethod> findByProviderAndProviderId(AuthProvider provider, String providerId);

}

