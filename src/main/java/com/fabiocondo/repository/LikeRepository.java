package com.fabiocondo.repository;

import com.fabiocondo.domain.Like;
import com.fabiocondo.domain.Post;
import com.fabiocondo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
    //Like findByPostAndUser(Post post, User user);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    Long countLikesByPostId(@Param("postId") Long postId);
}
