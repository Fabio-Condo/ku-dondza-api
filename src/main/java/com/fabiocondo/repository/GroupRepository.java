package com.fabiocondo.repository;

import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.Post;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:searchParam% OR g.description LIKE %:searchParam%")
    public Page<Group> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM Group g JOIN g.members u WHERE g.id = :groupId")
    Page<User> findMembersByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Group g JOIN g.members u WHERE g.id = :groupId")
    Long countMembersByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT g FROM Group g WHERE g.name LIKE %:query%")
    Page<Group> searchByQuery(@Param("query") String query, Pageable pageable);
}

