package com.fabiocondo.repository.query;

import com.fabiocondo.domain.FlashCard;
import com.fabiocondo.repository.filter.FlashCardFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlashCardRepositoryQuery {
    public Page<FlashCard> filter(FlashCardFilter flashCardFilter, Pageable pageable);
}
