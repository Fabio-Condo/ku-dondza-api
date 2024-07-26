package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Post;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SearchResultDTO;
import com.fabiocondo.repository.PostRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public Page<SearchResultDTO> search(String query, Pageable pageable) {
        // Adapte o tipo de retorno de acordo com suas necessidades
        Page<User> users = userRepository.searchByQuery(query, pageable);
        Page<Post> posts = postRepository.searchByQuery(query, pageable);

        // Combine os resultados e transforme em DTOs conforme necessário
        List<SearchResultDTO> results = new ArrayList<>();
        results.addAll(users.stream().map(user -> new SearchResultDTO("User", user.getFirstName() + " " + user.getLastName(), user.getProfileImageUrl())).collect(Collectors.toList()));
        results.addAll(posts.stream().map(post -> new SearchResultDTO("Post", post.getText(), post.getUrlFile())).collect(Collectors.toList()));

        // Crie uma página de resultados combinados
        return new PageImpl<>(results, pageable, results.size());
    }
}

