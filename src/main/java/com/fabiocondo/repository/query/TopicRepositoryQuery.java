package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.repository.filter.TopicFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TopicRepositoryQuery {
    public Page<Topic> filter(TopicFilter topicFilter, Pageable pageable);
}
