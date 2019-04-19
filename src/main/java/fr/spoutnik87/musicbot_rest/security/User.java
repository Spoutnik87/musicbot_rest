package fr.spoutnik87.musicbot_rest.security;

import lombok.Data;

@Data
public class User {
    private String email;
    private String password;
}
