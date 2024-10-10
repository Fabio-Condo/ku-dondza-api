package com.fabiocondo.controller;

import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.GroupNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.service.impl.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/groups")
public class GroupController {


    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Group>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> findById(@PathVariable("id") Long id) throws GroupNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Group> save(@RequestParam("name") String name,
                                      @RequestParam("description") String description,
                                      @RequestParam("file") MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(groupService.save(name, description, file));
    }

    @PutMapping
    public ResponseEntity<Group> update(@RequestParam("id") Long id,
                                        @RequestParam("name") String name,
                                        @RequestParam("description") String description,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws GroupNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(groupService.update(id, name, description, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ExameNotFoundException, GroupNotFoundException {
        groupService.delete(id);
        return response(HttpStatus.OK, "Group deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(groupService.getTotal());
    }

    @GetMapping("/{groupId}/members")
    public Page<User> getGroupMembers(@PathVariable Long groupId, Pageable pageable) throws GroupNotFoundException {
        return groupService.getMembersByGroupId(groupId, pageable);
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long userId) throws GroupNotFoundException {
        Group updatedGroup = groupService.addMemberToGroup(groupId, userId);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long userId) throws GroupNotFoundException {
        Group updatedGroup = groupService.removeMemberFromGroup(groupId, userId);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{groupId}/members/contains/{userId}")
    public ResponseEntity<Boolean> doesUserMemberOfGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean doesContain = groupService.doesUserMemberOfGroup(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }



    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

