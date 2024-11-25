package com.fabiocondo.service.impl;

import com.fabiocondo.domain.PollOption;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.PollOptionNotFoundException;
import com.fabiocondo.repository.PollOptionRepository;
import com.fabiocondo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class PollOptionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PollOptionRepository pollOptionRepository;
    private final UserRepository userRepository;

    public PollOptionService(PollOptionRepository pollOptionRepository, UserRepository userRepository) {
        this.pollOptionRepository = pollOptionRepository;
        this.userRepository = userRepository;
    }

    public Page<User> getPeopleWhoSelectedByOptionId(Long optionId, Pageable pageable) throws PollOptionNotFoundException {
        PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new PollOptionNotFoundException("Option not found with ID: " + optionId));
        return pollOptionRepository.findPeopleWhoSelectedByOptionId(option.getId(), pageable);
    }

    public PollOption addUserToOption(Long optionId, Long userId) throws PollOptionNotFoundException {
        PollOption option = pollOptionRepository.findById(optionId).orElseThrow(() -> new PollOptionNotFoundException("No option found by id: " + userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));
        option.getUsersWhoVoted().add(user);
        return pollOptionRepository.save(option);
    }

    public long countPeopleWhoSelectedByOptionId(Long optionId) {
        return pollOptionRepository.countPeopleWhoSelectedByOptionId(optionId);
    }

    public boolean checkIfSelected(Long optionId, Long userId) {
        PollOption option = pollOptionRepository.findById(optionId).orElse(null);
        if (option == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        return option.getUsersWhoVoted().contains(user);
    }

}
