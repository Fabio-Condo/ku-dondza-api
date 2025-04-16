package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.User;
import com.fabiocondo.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User dtoToDomainObject(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setUserId(userDTO.getUserId());
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setBio(userDTO.getBio());
        user.setProfileImageUrl(userDTO.getProfileImageUrl());
        user.setUserType(userDTO.getUserType());
        user.setPlan(userDTO.getPlan());
        return user;
    }

    public UserDTO domainToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserId(user.getUserId());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setBio(user.getBio());
        userDTO.setProfileImageUrl(user.getProfileImageUrl());
        userDTO.setUserType(user.getUserType());
        userDTO.setPlan(user.getPlan());
        return userDTO;
    }

    public Page<UserDTO> domainPageToDTOPage(Page<User> users, Pageable pageable) {
        return new PageImpl<>(users.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, users.getTotalElements());
    }
}
