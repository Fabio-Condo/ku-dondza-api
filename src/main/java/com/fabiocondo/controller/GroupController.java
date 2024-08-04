package com.fabiocondo.controller;

import com.fabiocondo.domain.Group;
import com.fabiocondo.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        Group createdGroup = groupService.createGroup(group);
        return ResponseEntity.ok(createdGroup);
    }

    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        Optional<Group> group = groupService.getGroupById(id);
        return group.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @RequestBody Group groupDetails) {
        Group updatedGroup = groupService.updateGroup(id, groupDetails);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        Group updatedGroup = groupService.addMemberToGroup(groupId, userId);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        Group updatedGroup = groupService.removeMemberFromGroup(groupId, userId);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }
}

