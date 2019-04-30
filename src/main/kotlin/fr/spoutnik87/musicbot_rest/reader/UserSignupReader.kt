package fr.spoutnik87.musicbot_rest.reader

data class UserSignupReader(
        var email: String,
        var nickname: String,
        var firstname: String,
        var lastname: String,
        var password: String
) {
}