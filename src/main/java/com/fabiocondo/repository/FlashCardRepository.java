package com.fabiocondo.repository;

import com.fabiocondo.domain.FlashCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashCardRepository extends JpaRepository<FlashCard, Long> {

    List<FlashCard> findByTopic_Id(Long topicId);
    List<FlashCard> findByTopicTopicId(Long topicId);
    //List<FlashCard> findByTopicTopicId(String topicId);


}
