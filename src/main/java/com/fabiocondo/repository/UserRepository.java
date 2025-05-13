package com.fabiocondo.repository;


import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.repository.query.UserRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryQuery {

    User findUserByEmail(String email);

    Optional<User> findUserByUserId(String userId);

    List<User> findByUserType(UserType userType);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:searchParam% OR u.email LIKE %:searchParam% OR u.role LIKE %:searchParam%")
    public Page<User> findByAnyProperty(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:query%")
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT oc FROM User u JOIN u.subscribedCourses oc WHERE u.id = :userId")
    Page<Course> findSubscribedCoursesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(oc) FROM User u JOIN u.subscribedCourses oc WHERE u.id = :userId")
    Long countSubscribedCoursesByUserId(@Param("userId") Long userId);

    public long countByIsActiveTrue();

    @Query("SELECT a FROM User u JOIN u.savedArticles a WHERE u.id = :userId")
    Page<Article> findSavedArticlesByUserId(@Param("userId") Long userId, Pageable pageable);

}
