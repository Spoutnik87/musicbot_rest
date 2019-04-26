package fr.spoutnik87.musicbot_rest.constant;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    CREATE_MEDIA("CREATE_MEDIA"),
    DELETE_MEDIA("DELETE_MEDIA"),
    READ_MEDIA("READ_MEDIA"),
    CHANGE_MODE("CHANGE_MODE"),
    PLAY_MEDIA("PLAY_MEDIA"),
    STOP_MEDIA("STOP_MEDIA"),
    CREATE_CATEGORY("CREATE_CATEGORY"),
    DELETE_CATEGORY("DELETE_CATEGORY");

    private String value;

    PermissionEnum(String value) {
        this.value = value;
    }
}
