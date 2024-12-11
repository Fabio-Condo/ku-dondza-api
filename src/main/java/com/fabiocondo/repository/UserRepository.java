package com.fabiocondo.repository;


import com.fabiocondo.domain.CommentLike;
import com.fabiocondo.domain.Post;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User findUserByEmail(String email);

    Page<User> findByUserType(UserType userType, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:searchParam% OR u.lastName LIKE %:searchParam% OR u.username LIKE %:searchParam% OR u.role LIKE %:searchParam%")
    public Page<User> findByAnyProperty(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:query% OR u.lastName LIKE %:query%")
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT u.savedPosts FROM User u WHERE u.id = :userId")
    Page<Post> findSavedPostsByUser(Long userId, Pageable pageable);

    @Query("SELECT sp FROM User u JOIN u.savedPosts sp WHERE u.id = :userId")
    Page<Post> findSavedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT fr FROM User u JOIN u.friends fr WHERE u.id = :userId")
    Page<User> findFriendsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM User u JOIN u.friends f WHERE u.id = :userId")
    Long countFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT fr FROM User u JOIN u.friendRequests fr WHERE u.id = :userId")
    Page<User> findFriendRequestsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(fr) FROM User u JOIN u.friendRequests fr WHERE u.id = :userId")
    Long countFriendRequestsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(oc) FROM User u JOIN u.subscribedOnlineCourses oc WHERE u.id = :userId")
    Long countSubscribedOnlineCoursesByOnlineUserId(@Param("userId") Long userId);

    public long countByIsActiveTrue();

}
