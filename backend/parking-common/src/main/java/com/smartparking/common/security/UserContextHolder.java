package com.smartparking.common.security;

/**
 * Stores authenticated user information for the current request thread.
 *
 * <p>Interceptor sets it; service layer can read it for auditing/authorization decisions.</p>
 */
public final class UserContextHolder {
    private static final ThreadLocal<UserContext> CTX = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        CTX.set(context);
    }

    public static UserContext get() {
        return CTX.get();
    }

    public static void clear() {
        CTX.remove();
    }
}

