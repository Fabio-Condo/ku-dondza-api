package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.dto.UserSubjectRankingDTO;

public class UserSubjectRankingMapper {

    public static UserSubjectRankingDTO toDTO(UserSubjectScore entity) {

        if (entity == null) return null;

        return new UserSubjectRankingDTO(
                entity.getUser().getId(),
                entity.getUser().getFullName(),
                entity.getUser().getProfileImageUrl(),
                entity.getScore(),
                10D // Calcular a taxa de testes finalizados
        );
    }
}
