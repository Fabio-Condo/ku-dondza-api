package com.fabiocondo.repository;

import com.fabiocondo.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findById(String username, Pageable pageable);
    Page<Post> findByUserId(Long userId, Pageable pageable);
    Page<Post> findAllByCommentsParentCommentIsNotNull(Pageable pageable);
    Page<Post> findAllByCommentsParentCommentIsNull(Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.text LIKE %:query%")
    Page<Post> searchByQuery(@Param("query") String query, Pageable pageable);
    public Page<Post> findByGroupId(Long groupId, Pageable pageable);
    //Page<Post> findByContentContainingIgnoreCase(String query, Pageable pageable);

}
