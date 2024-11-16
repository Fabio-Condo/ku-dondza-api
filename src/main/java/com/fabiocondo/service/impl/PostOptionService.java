package com.fabiocondo.service.impl;

import com.fabiocondo.domain.PostOption;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.PostOptionNotFoundException;
import com.fabiocondo.repository.PostOptionRepository;
import com.fabiocondo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class PostOptionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PostOptionRepository postOptionRepository;
    private final UserRepository userRepository;

    public PostOptionService(PostOptionRepository postOptionRepository, UserRepository userRepository) {
        this.postOptionRepository = postOptionRepository;
        this.userRepository = userRepository;
    }

    public Page<User> getPeopleWhoSelectedByOptionId(Long optionId, Pageable pageable) throws PostOptionNotFoundException {
        PostOption option = postOptionRepository.findById(optionId)
                .orElseThrow(() -> new PostOptionNotFoundException("Option not found with ID: " + optionId));
        return postOptionRepository.findPeopleWhoSelectedByOptionId(option.getId(), pageable);
    }

    public PostOption addUserToOption(Long optionId, Long userId) throws PostOptionNotFoundException {
        PostOption option = postOptionRepository.findById(optionId).orElseThrow(() -> new PostOptionNotFoundException("No option found by id: " + userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));
        option.getPeopleWhoSelected().add(user);
        return postOptionRepository.save(option);
    }

    public long countPeopleWhoSelectedByOptionId(Long optionId) {
        return postOptionRepository.countPeopleWhoSelectedByOptionId(optionId);
    }

    public boolean checkIfSelected(Long optionId, Long userId) {
        PostOption option = postOptionRepository.findById(optionId).orElse(null);
        if (option == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        return option.getPeopleWhoSelected().contains(user);
    }

}
