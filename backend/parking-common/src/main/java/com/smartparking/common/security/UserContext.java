package com.smartparking.common.security;

import lombok.Value;

@Value
public class UserContext {
    long userId;
    UserRole role;
}

