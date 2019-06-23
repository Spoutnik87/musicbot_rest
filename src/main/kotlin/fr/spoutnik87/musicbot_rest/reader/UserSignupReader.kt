package fr.spoutnik87.musicbot_rest.reader

data class UserSignupReader(
        val email: String,
        val nickname: String,
        val firstname: String,
        val lastname: String,
        val password: String
)