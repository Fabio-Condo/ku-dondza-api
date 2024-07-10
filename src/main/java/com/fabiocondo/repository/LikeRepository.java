package com.fabiocondo.repository;

import com.fabiocondo.domain.Like;
import com.fabiocondo.domain.Post;
import com.fabiocondo.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
    //Like findByPostAndUser(Post post, User user);

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
