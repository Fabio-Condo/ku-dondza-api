package com.fabiocondo.controller;

import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.*;
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
    public ResponseEntity<Page<Group>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findAll(searchParam, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> findById(@PathVariable("id") Long id) throws GroupNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findById(id));
    }

    @GetMapping("/find-by-groupId/{groupId}")
    public ResponseEntity<Group> findGroupByGroupId(@PathVariable("groupId") String groupId) throws GroupNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findGroupByGroupId(groupId));
    }

    @PostMapping
    public ResponseEntity<Group> save(@RequestParam("name") String name,
                                      @RequestParam("description") String description,
                                      @RequestParam("file") MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException, UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(groupService.save(name, description, file));
    }

    @PutMapping
    public ResponseEntity<Group> update(@RequestParam("id") Long id,
                                        @RequestParam("name") String name,
                                        @RequestParam("description") String description,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws GroupNotFoundException, UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(groupService.update(id, name, description, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ExamNotFoundException, GroupNotFoundException {
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
    public ResponseEntity<Boolean> checkMembership(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean doesContain = groupService.checkMembership(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }

    @GetMapping("/{groupId}/members/total")
    public ResponseEntity<Long> countMembersByGroupId(@PathVariable Long groupId){
        return ResponseEntity.status(HttpStatus.OK).body(groupService.countMembersByGroupId(groupId));
    }

    @GetMapping("/{groupId}/administrators")
    public Page<User> getAdministratorsByGroupId(@PathVariable Long groupId, Pageable pageable) throws GroupNotFoundException {
        return groupService.getAdministratorsByGroupId(groupId, pageable);
    }

    @PostMapping("/{groupId}/administrators/{userId}")
    public ResponseEntity<Group> addMemberToGroupAdministrators(@PathVariable Long groupId, @PathVariable Long userId) throws GroupNotFoundException {
        Group updatedGroup = groupService.addMemberToGroupAdministrators(groupId, userId);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{groupId}/administrators/{userId}")
    public ResponseEntity<Group> removeMemberFromGroupAdministrators(@PathVariable Long groupId, @PathVariable Long userId) throws GroupNotFoundException {
        Group updatedGroup = groupService.removeMemberFromGroupAdministrators(groupId, userId);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{groupId}/administrators/contains/{userId}")
    public ResponseEntity<Boolean> checkIsAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean doesContain = groupService.checkIsAdmin(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }

    @GetMapping("/{groupId}/administrators/total")
    public ResponseEntity<Long> countAdministratorsByGroupId(@PathVariable Long groupId){
        return ResponseEntity.status(HttpStatus.OK).body(groupService.countAdministratorsByGroupId(groupId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

