package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.CommentDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.CommentLikeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    private final CommentLikeService commentLikeService;
    private final UserRepository userRepository;


    public CommentMapper(CommentLikeService commentLikeService, UserRepository userRepository) {
        this.commentLikeService = commentLikeService;
        this.userRepository = userRepository;
    }

    public Comment dtoToDomainObject(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setId(commentDTO.getId());
        comment.setContent(commentDTO.getContent());
        comment.setUser(commentDTO.getUser());
        comment.setQuestion(commentDTO.getQuestion());
        return comment;
    }

    public CommentDTO domainToDTO(Comment comment, Long currentUserId) throws UserNotFoundException {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setCreatedAt(comment.getCreatedAt());
        commentDTO.setUser(comment.getUser());
        commentDTO.setQuestion(comment.getQuestion());

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            commentDTO.setLikedByUser(commentLikeService.isCommentLikedByUser(comment.getId(), currentUserId));
        }

        commentDTO.setNumberOfLikes(commentLikeService.countLikesByCommentId(comment.getId()));
        return commentDTO;
    }

    public Page<CommentDTO> domainPageToDTOPage(Page<Comment> comments, Long currentUserId, Pageable pageable) {

        return new PageImpl<>(comments.stream()
                .map(comment -> {
                    try {
                        return domainToDTO(comment, currentUserId);
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException("Erro ao mapear comment com ID " + comment.getId(), e);
                    }
                })
                .collect(Collectors.toList()), pageable, comments.getTotalElements());
    }

}
