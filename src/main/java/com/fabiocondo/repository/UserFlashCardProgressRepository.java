package com.fabiocondo.repository;

import com.fabiocondo.domain.UserFlashCardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFlashCardProgressRepository
        extends JpaRepository<UserFlashCardProgress, Long> {

    //Optional<UserFlashCardProgress> findByUserUserIdAndFlashCardId(Long userId, Long flashCardId);
    Optional<UserFlashCardProgress> findByUser_IdAndFlashCard_Id(
            Long userId,
            Long flashCardId
    );

    List<UserFlashCardProgress>
    findByUserUserId(Long userId);

}
