package com.fabiocondo.repository;


import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    @Query("SELECT u FROM user u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR u.username LIKE %:name% OR u.role LIKE %:name%")
    public Page<User> findByAnyProperty(@Param("name") String name, Pageable pageable);

    public long countByIsActiveTrue();
}
