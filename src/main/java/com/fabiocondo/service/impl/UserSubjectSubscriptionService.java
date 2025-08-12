package com.fabiocondo.service.impl;

import com.fabiocondo.domain.UserSubjectSubscription;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.SubscriptionExistException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.exception.domain.UserSubjectSubscriptionNotFoundException;
import com.fabiocondo.repository.SubjectRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.UserSubjectSubscriptionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserSubjectSubscriptionService {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final UserSubjectSubscriptionRepository userSubjectSubscriptionRepository;

    public UserSubjectSubscriptionService(UserRepository userRepository, SubjectRepository subjectRepository, UserSubjectSubscriptionRepository userSubjectSubscriptionRepository) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.userSubjectSubscriptionRepository = userSubjectSubscriptionRepository;
    }

    public UserSubjectSubscription addSubjectToUser(UserSubjectSubscription userSubjectSubscription) throws UserNotFoundException, SubjectNotFoundException, SubscriptionExistException {
        boolean isEnrolled = isUserEnrolledInSubject(userSubjectSubscription.getUser().getId(), userSubjectSubscription.getSubject().getId());
        if(isEnrolled){
            throw new SubscriptionExistException("Subscription already exists");
        }
        userSubjectSubscription.setDate(new Date());
        return userSubjectSubscriptionRepository.save(userSubjectSubscription);
    }

    public UserSubjectSubscription updateUserSubjectSubscription(Long userSubjectId, UserSubjectSubscription userSubjectSubscription) throws UserSubjectSubscriptionNotFoundException {
        UserSubjectSubscription existeUserSubjectSubscription = userSubjectSubscriptionRepository.findById(userSubjectId)
                .orElseThrow(() -> new UserSubjectSubscriptionNotFoundException("User Subject Subscription not found by id " + userSubjectId));
        BeanUtils.copyProperties(userSubjectSubscription, existeUserSubjectSubscription, "id");
        return userSubjectSubscriptionRepository.save(existeUserSubjectSubscription);
    }

    public UserSubjectSubscription updateSubscriptionByUserAndSubject(Long userId, Long subjectId, Date startDate) throws UserSubjectSubscriptionNotFoundException {
        UserSubjectSubscription userSubjectSubscription = userSubjectSubscriptionRepository.findByUserIdAndSubjectId(userId, subjectId)
                .orElseThrow(() -> new UserSubjectSubscriptionNotFoundException("User Subject Subscription not found"));

        if (startDate != null) {
            userSubjectSubscription.setDate(startDate);
        }

        return userSubjectSubscriptionRepository.save(userSubjectSubscription);
    }

    public void removeSubscription(Long userSubjectSubscriptionId) throws UserSubjectSubscriptionNotFoundException {
        UserSubjectSubscription userSubjectSubscription = userSubjectSubscriptionRepository.findById(userSubjectSubscriptionId)
                .orElseThrow(() -> new UserSubjectSubscriptionNotFoundException("User Subject Subscription not found by id " + userSubjectSubscriptionId));

        userSubjectSubscriptionRepository.delete(userSubjectSubscription);
    }

    public Page<UserSubjectSubscription> getSubjectsByUser(Long userId, Pageable pageable) {
        return userSubjectSubscriptionRepository.findByUserId(userId, pageable);
    }

    public Page<UserSubjectSubscription> getUsersBySubject(Long subjectId, Pageable pageable) {
        return userSubjectSubscriptionRepository.findBySubjectId(subjectId, pageable);
    }

    public boolean isUserEnrolledInSubject(Long userId, Long subjectId) throws UserNotFoundException, SubjectNotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found bay id " + userId);
        }
        if (!subjectRepository.existsById(subjectId)) {
            throw new SubjectNotFoundException("Subject not found by id " + subjectId);
        }
        return userSubjectSubscriptionRepository.existsByUserIdAndSubjectId(userId, subjectId);
    }
}

    /*

    // Spring nao consegui o id como chave primaria, entao deve criar manualmente essa table, remova a criada por spring e crie esta, ou simplesmente adicione o AUTO_INCREMENT PRIMARY KEY no id

    CREATE TABLE user_subject_subscription (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        subject_id BIGINT NOT NULL,
        date DATE,
        CONSTRAINT fk_user_subject_subscription_user
            FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
        CONSTRAINT fk_user_subject_subscription_subject
            FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
    );

    */
