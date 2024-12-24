package com.fabiocondo.repository;

import com.fabiocondo.domain.PollOption;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    @Query("SELECT u FROM PollOption o JOIN o.usersWhoVoted u WHERE o.id = :optionId")
    Page<User> findPeopleWhoSelectedByOptionId(@Param("optionId") Long optionId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM PollOption o JOIN o.usersWhoVoted u WHERE o.id = :optionId")
    Long countPeopleWhoSelectedByOptionId(@Param("optionId") Long optionId);

    PollOption findByUsersWhoVotedContaining(User user);

    Optional<PollOption> findByPostIdAndUsersWhoVoted_Id(Long postId, Long userId);

    boolean existsByPostIdAndUsersWhoVoted_Id(Long postId, Long userId);
}
