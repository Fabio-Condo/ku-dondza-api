package com.fabiocondo.repository;


import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User findUserByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR u.username LIKE %:name% OR u.role LIKE %:name%")
    public Page<User> findByAnyProperty(@Param("name") String name, Pageable pageable);

    public long countByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:query% OR u.lastName LIKE %:query%")
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);

    //Page<User> findByNameContainingIgnoreCase(String query, Pageable pageable);
}
