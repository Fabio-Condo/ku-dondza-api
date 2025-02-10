package com.fabiocondo.service;

import com.fabiocondo.domain.Notification;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.NotificationNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.NotificationRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;


    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, UserRepository userRepository1) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository1;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification createNotification(Long userId, String message, String type) throws UserNotFoundException {
        Notification notification = new Notification();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + userId));

        notification.setUser(user); // Assumindo que User tem um construtor com ID
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId) throws NotificationNotFoundException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("No Notification found by id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}

