package com.fabiocondo.enumeration;

public enum NotificationType {
    COMPETITION_STARTED,
    COMPETITION_FINISHED,
    COMPETITION_INVITE,
    COMPETITION_WINNER,
    FRIEND_REQUEST,       // Pedido de amizade
    FRIEND_ACCEPTED,      // Pedido de amizade aceito
    POST_LIKE,           // Alguém curtiu um post seu
    COMMENT_LIKE,        // Alguém curtiu um comentário seu
    COMMENT_REPLY,       // Responderam seu comentário
    POST_COMMENT,        // Alguém comentou no seu post
    MENTION,             // Foi mencionado em um post/comentário
    GROUP_INVITE,        // Foi convidado para um grupo
    MESSAGE_RECEIVED     // Recebeu uma nova mensagem no chat
}

