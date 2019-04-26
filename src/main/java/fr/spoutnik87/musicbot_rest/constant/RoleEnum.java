package fr.spoutnik87.musicbot_rest.constant;

import lombok.Getter;

@Getter
public enum RoleEnum {
    USER("USER"),
    ADMIN("ADMIN");

    private String name;

    RoleEnum(String name) {
        this.name = name;
    }
}
