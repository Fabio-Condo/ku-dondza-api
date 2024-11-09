package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SearchResultDTO;
import com.fabiocondo.repository.GroupRepository;
import com.fabiocondo.repository.InstitutionRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// SearchService.java
@Service
public class SearchService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final InstitutionRepository institutionRepository;

    public SearchService(UserRepository userRepository, GroupRepository groupRepository, InstitutionRepository institutionRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.institutionRepository = institutionRepository;
    }

    public Page<SearchResultDTO> search(String query, Pageable pageable) {
        // Adapte o tipo de retorno de acordo com suas necessidades
        Page<User> users = userRepository.searchByQuery(query, pageable);
        Page<Group> groups = groupRepository.searchByQuery(query, pageable);
        Page<Institution> institutions = institutionRepository.searchByQuery(query, pageable);

        // Combine os resultados e transforme em DTOs conforme necessário
        List<SearchResultDTO> results = new ArrayList<>();
        results.addAll(users.stream().map(user -> new SearchResultDTO("User", user.getFirstName() + " " + user.getLastName(), user.getProfileImageUrl(), user.getId().toString())).collect(Collectors.toList()));
        results.addAll(groups.stream().map(group -> new SearchResultDTO("Group", group.getName(), group.getUrlFile(), group.getId().toString())).collect(Collectors.toList()));
        results.addAll(institutions.stream().map(institution -> new SearchResultDTO("Institution", institution.getName(), institution.getUrlFile(), institution.getId().toString())).collect(Collectors.toList()));

        // Crie uma página de resultados combinados
        return new PageImpl<>(results, pageable, results.size());
    }
}

