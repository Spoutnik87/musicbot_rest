package fr.spoutnik87.musicbot_rest.reader

data class UserUpdateReader(
        val nickname: String?,
        val firstname: String?,
        val lastname: String?
)