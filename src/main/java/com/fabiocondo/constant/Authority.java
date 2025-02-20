package com.fabiocondo.constant;

public class Authority {
    public static final String[] USER_AUTHORITIES = { "user:read", "book:read", "course:read", "question:read", "quiz:create", "quiz:delete"};
    public static final String[] ADMIN_AUTHORITIES = { "user:read", "user:create", "user:update", "book:read", "book:create", "book:update", "question:read", "question:create", "question:update", "subject:read", "subject:create", "subject:update", "topic:read", "topic:create", "topic:update", "blog:read", "blog:create", "blog:update"};
    public static final String[] SUPER_ADMIN_AUTHORITIES = { "user:read", "user:create", "user:update", "user:delete", "book:read", "book:create", "book:update", "book:delete", "question:read", "question:create", "question:update", "question:delete", "subject:read", "subject:create", "subject:update", "subject:delete", "topic:read", "topic:create", "topic:update", "topic:delete", "blog:read", "blog:create", "blog:update", "blog:delete"};
}
