package com.fabiocondo.repository;

import com.fabiocondo.domain.PostOption;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostOptionRepository extends JpaRepository<PostOption, Long> {

    @Query("SELECT u FROM PostOption o JOIN o.peopleWhoSelected u WHERE o.id = :optionId")
    Page<User> findPeopleWhoSelectedByOptionId(@Param("optionId") Long optionId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM PostOption o JOIN o.peopleWhoSelected u WHERE o.id = :optionId")
    Long countPeopleWhoSelectedByOptionId(@Param("optionId") Long optionId);
}
