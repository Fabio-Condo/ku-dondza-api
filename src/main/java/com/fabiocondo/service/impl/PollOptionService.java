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

import javax.transaction.Transactional;


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

    public PollOption toggleUserVote(Long optionId, Long userId) throws PollOptionNotFoundException {
        // Busca pela opção de enquete com o ID fornecido
        PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new PollOptionNotFoundException("No option found by id: " + optionId));

        // Busca pelo usuário com o ID fornecido
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));

        // Verifica se o usuário já votou em alguma opção do mesmo post
        PollOption currentVote = pollOptionRepository.findByPostIdAndUsersWhoVoted_Id(option.getPost().getId(), userId)
                .orElse(null);

        if (currentVote != null && !currentVote.equals(option)) {
            // Se o usuário já votou em uma opção diferente do mesmo post, remove o voto anterior
            currentVote.getUsersWhoVoted().remove(user);
            pollOptionRepository.save(currentVote);
        }

        // Adiciona o voto do usuário na nova opção
        option.getUsersWhoVoted().add(user);

        // Salva e retorna a opção com o voto atualizado
        return pollOptionRepository.save(option);
    }

    @Transactional
    public PollOption removeUserVote(Long postId, Long userId) throws PollOptionNotFoundException, UsernameNotFoundException {
        // Busca o usuário com o ID fornecido
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));

        // Busca a opção de enquete do post em que o usuário votou
        PollOption currentVote = pollOptionRepository.findByPostIdAndUsersWhoVoted_Id(postId, userId)
                .orElseThrow(() -> new PollOptionNotFoundException("No vote found for user " + userId + " in post " + postId));

        // Remove o voto do usuário da opção
        currentVote.getUsersWhoVoted().remove(user);

        // Salva a opção de enquete com o voto removido
        return pollOptionRepository.save(currentVote);
    }

    public boolean hasUserVoted(Long postId, Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));
        return pollOptionRepository.existsByPostIdAndUsersWhoVoted_Id(postId, user.getId());
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
