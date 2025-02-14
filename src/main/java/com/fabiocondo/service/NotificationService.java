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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) throws NotificationNotFoundException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("No Notification found by id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
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
    public void createCompetitionStartedNotification(Long competitionId) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        Set<User> participants = competition.getParticipants(); // Notifica todos os participantes

        for (User participant : participants) {
            String message = "A competição '" + competition.getTitle() + "' está iniciada! Participe agora.";
            createNotification(participant, null, competition, message, NotificationType.COMPETITION_STARTED);
        }
    }

    @Async
    public void createCompetitionFinishedNotification(Long competitionId) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        Set<User> participants = competition.getParticipants(); // Notifica todos os participantes

        for (User participant : participants) {
            String message = "A competição '" + competition.getTitle() + "' está finalizada!.";
            createNotification(participant, null, competition, message, NotificationType.COMPETITION_FINISHED);
        }
    }

    @Async
    public void createCompetitionParticipantRemovedNotification(Long userId, Long competitionId) throws UserNotFoundException, CompetitionNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + userId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        String message = "Você foi removido da competição '" + competition.getTitle() + "'.";
        createNotification(user, null, competition, message, NotificationType.COMPETITION_PARTICIPANT_REMOVED);
    }

    @Async
    public void createCompetitionInviteNotification(Long senderId, Long receiverId, Long competitionId) throws UserNotFoundException, CompetitionNotFoundException {
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
    public void createCompetitionDisqualifiedNotification(Long userId, Long competitionId) throws UserNotFoundException, CompetitionNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + userId));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No Competition found by id: " + competitionId));

        String message = "Você foi desclassificado da competição '" + competition.getTitle() + "'.";
        createNotification(user, null, competition, message, NotificationType.COMPETITION_DISQUALIFIED);
    }

    @Async
    public void createFriendRequestNotification(Long senderId, Long receiverId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + receiverId));

        String message = sender.getFirstName() + " enviou um pedido de amizade.";
        createNotification(receiver, sender, null, message, NotificationType.FRIEND_REQUEST);
    }

    @Async
    public void createFriendAcceptNotification(Long senderId, Long receiverId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + receiverId));

        String message = receiver.getFirstName() + " aceitou seu pedido de amizade.";
        createNotification(sender, receiver, null, message, NotificationType.FRIEND_ACCEPTED);
    }

    @Async
    public void createFriendAcceptNotificationForReceiver(Long senderId, Long receiverId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + receiverId));

        String message = "Agora você e " + sender.getFirstName() + " são amigos!";
        createNotification(receiver, sender, null, message, NotificationType.FRIEND_ACCEPTED_RECEIVER);
    }

    @Async
    public void createPostLikeNotification(Long senderId, Long postOwnerId, Long postId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User postOwner = userRepository.findById(postOwnerId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + postOwnerId));

        String message = sender.getFirstName() + " curtiu seu post.";
        createNotification(postOwner, sender, null, message, NotificationType.POST_LIKE);
    }

    @Async
    public void createPostCommentNotification(Long senderId, Long postOwnerId, Long postId) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User postOwner = userRepository.findById(postOwnerId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + postOwnerId));

        String message = sender.getFirstName() + " comentou no seu post.";
        createNotification(postOwner, sender, null, message, NotificationType.POST_COMMENT);
    }

    @Async
    public void createMentionNotification(Long senderId, Long mentionedUserId, Long referenceId, boolean isPost) throws UserNotFoundException {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + senderId));

        User mentionedUser = userRepository.findById(mentionedUserId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + mentionedUserId));

        String message = sender.getFirstName() + " mencionou você em um " + (isPost ? "post" : "comentário") + ".";
        createNotification(mentionedUser, sender, null, message, NotificationType.MENTION);
    }

    private void createNotification(User user, User sender, Competition competition, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setSender(sender);
        notification.setCompetition(competition);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);
    }
}
