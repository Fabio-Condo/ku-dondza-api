package com.fabiocondo.repository.query;

import com.fabiocondo.domain.User;
import com.fabiocondo.repository.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryQuery {
    public Page<User> filter(UserFilter bookFilter, Pageable pageable);
}
