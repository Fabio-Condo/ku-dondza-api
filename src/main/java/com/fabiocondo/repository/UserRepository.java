package com.fabiocondo.repository;


import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User findUserByEmail(String email);

    List<User> findByUserType(UserType userType);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:searchParam% OR u.lastName LIKE %:searchParam% OR u.username LIKE %:searchParam% OR u.role LIKE %:searchParam%")
    public Page<User> findByAnyProperty(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:query% OR u.lastName LIKE %:query%")
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT oc FROM User u JOIN u.subscribedOnlineCourses oc WHERE u.id = :userId")
    Page<OnlineCourse> findSubscribedOnlineCoursesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(oc) FROM User u JOIN u.subscribedOnlineCourses oc WHERE u.id = :userId")
    Long countSubscribedOnlineCoursesByUserId(@Param("userId") Long userId);

    public long countByIsActiveTrue();

}
