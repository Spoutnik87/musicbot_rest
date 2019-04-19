package fr.spoutnik87.musicbot_rest.security;

import org.springframework.beans.factory.annotation.Value;

public abstract class SecurityConstants {

    @Value("${security.secret}")
    static String SECRET;
    static final long EXPIRATION_TIME = 864_000_000;
    static final String TOKEN_PREFIX = "Bearer ";
    static final String HEADER_STRING = "Authorization";
    static final String SIGN_UP_URL = "/user";
}
