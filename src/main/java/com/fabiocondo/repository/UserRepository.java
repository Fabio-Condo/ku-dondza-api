package com.fabiocondo.repository;


import com.fabiocondo.domain.Post;
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

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:query% OR u.lastName LIKE %:query%")
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT u.savedPosts FROM User u WHERE u.id = :userId")
    Page<Post> findSavedPostsByUser(Long userId, Pageable pageable);

    @Query("SELECT sp FROM User u JOIN u.savedPosts sp WHERE u.id = :userId")
    Page<Post> findSavedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT fr FROM User u JOIN u.friends fr WHERE u.id = :userId")
    Page<User> findFriendsByUserId(@Param("userId") Long userId, Pageable pageable);

    public long countByIsActiveTrue();

}
