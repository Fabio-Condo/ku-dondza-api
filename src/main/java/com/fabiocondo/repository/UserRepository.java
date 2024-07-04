package com.fabiocondo.repository;


import com.fabiocondo.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    public long countByIsActiveTrue();
}
