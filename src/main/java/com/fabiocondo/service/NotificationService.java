package com.fabiocondo.service;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Notification;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.NotificationType;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.NotificationNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.NotificationRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, CompetitionRepository competitionRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public void markAsRead(Long notificationId) throws NotificationNotFoundException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("No Notification found by id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Async
    public void createParticipationRequestNotification(Long userSenderId, Long competitionId) throws UserNotFoundException, CompetitionNotFoundException {
        User userSender = userRepository.findById(userSenderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + userSenderId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        String message = userSender.getFirstName() + " enviou um pedido de participação.";

        for (User userAdmin : competition.getAdministrators()) {
            createNotification(userAdmin, userSender, competition, message, NotificationType.PARTICIPATION_REQUEST);
        }
    }

    @Async // Uso de @Async Para não bloquear a execução principal ao enviar as notificações
    public void notifyCompetitionStarted(Long competitionId) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        Set<User> usuarios = competition.getParticipants(); // Notifica todos os participantes

        for (User user : usuarios) {
            String message = "A competição '" + competition.getTitle() + "' está iniciada! Participe agora.";
            createNotification(user, null, competition, message, NotificationType.COMPETITION_STARTED);
        }
    }

    @Async
    public void notifyCompetitionFinished(Long competitionId) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        Set<User> usuarios = competition.getParticipants(); // Notifica todos os participantes

        for (User user : usuarios) {
            String message = "A competição '" + competition.getTitle() + "' está finalizada!.";
            createNotification(user, null, competition, message, NotificationType.COMPETITION_FINISHED);
        }
    }

    @Async
    public void notifyCompetitionInvite(Long senderId, Long receiverId, Long competitionId) throws UserNotFoundException, CompetitionNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + receiverId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        String message = sender.getFirstName() + " " + sender.getLastName() + " convidou você para participar da competição '" + competition.getTitle() + "'.";
        createNotification(receiver, sender, competition, message, NotificationType.COMPETITION_INVITE);
    }

    @Async
    public Notification createPostLikeNotification(Long senderId, Long postOwnerId, Long postId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User postOwner = userRepository.findById(postOwnerId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + postOwnerId));

        String message = sender.getFirstName() + " curtiu seu post.";
        return createNotification(postOwner, sender, null, message, NotificationType.POST_LIKE);
    }

    @Async
    public Notification createFriendRequestNotification(Long senderId, Long receiverId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + receiverId));

        String message = sender.getFirstName() + " enviou um pedido de amizade.";
        return createNotification(receiver, sender, null, message, NotificationType.FRIEND_REQUEST);
    }

    @Async
    public Notification createFriendAcceptNotification(Long senderId, Long receiverId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + receiverId));

        String message = receiver.getFirstName() + " aceitou seu pedido de amizade.";
        return createNotification(sender, receiver, null, message, NotificationType.FRIEND_ACCEPTED);
    }

    @Async
    public Notification createPostCommentNotification(Long senderId, Long postOwnerId, Long postId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User postOwner = userRepository.findById(postOwnerId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + postOwnerId));

        String message = sender.getFirstName() + " comentou no seu post.";
        return createNotification(postOwner, sender, null, message, NotificationType.POST_COMMENT);
    }

    @Async
    public Notification createMentionNotification(Long senderId, Long mentionedUserId, Long referenceId, boolean isPost) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User mentionedUser = userRepository.findById(mentionedUserId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + mentionedUserId));

        String message = sender.getFirstName() + " mencionou você em um " + (isPost ? "post" : "comentário") + ".";
        return createNotification(mentionedUser, sender, null, message, NotificationType.MENTION);
    }

    private Notification createNotification(User user, User sender, Competition competition, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setSender(sender);
        notification.setCompetition(competition);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }
}
