package fr.spoutnik87.musicbot_rest.constant

enum class PermissionEnum(
        val value: String
) {
    CREATE_CONTENT("CREATE_CONTENT"),
    DELETE_CONTENT("DELETE_CONTENT"),
    READ_CONTENT("READ_CONTENT"),
    CHANGE_MODE("CHANGE_MODE"),
    PLAY_MEDIA("PLAY_MEDIA"),
    STOP_MEDIA("STOP_MEDIA"),
    CREATE_CATEGORY("CREATE_CATEGORY"),
    DELETE_CATEGORY("DELETE_CATEGORY")
}