package com.fabiocondo.service;

import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.User;
import com.fabiocondo.repository.GroupRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    // Criar grupo
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    // Listar todos os grupos
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // Obter grupo por ID
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    // Atualizar grupo
    public Group updateGroup(Long id, Group groupDetails) {
        Optional<Group> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            group.setDescription(groupDetails.getDescription());
            return groupRepository.save(group);
        } else {
            return null; // ou lançar uma exceção apropriada
        }
    }

    // Excluir grupo
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    // Adicionar membro ao grupo
    public Group addMemberToGroup(Long groupId, Long userId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalGroup.isPresent() && optionalUser.isPresent()) {
            Group group = optionalGroup.get();
            User user = optionalUser.get();
            group.getMembers().add(user);
            return groupRepository.save(group);
        } else {
            return null; // ou lançar uma exceção apropriada
        }
    }

    // Remover membro do grupo
    public Group removeMemberFromGroup(Long groupId, Long userId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalGroup.isPresent() && optionalUser.isPresent()) {
            Group group = optionalGroup.get();
            User user = optionalUser.get();
            group.getMembers().remove(user);
            return groupRepository.save(group);
        } else {
            return null; // ou lançar uma exceção apropriada
        }
    }
}
