package fr.spoutnik87.musicbot_rest.constant

enum class RoleEnum(
        val value: String,
        val lvl: Int
) {
    ADMIN("ADMIN", 1),
    USER("USER", 2),
    BOT("BOT", 3)
}