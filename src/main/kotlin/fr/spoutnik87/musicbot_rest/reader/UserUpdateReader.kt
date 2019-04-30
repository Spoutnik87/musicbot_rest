package fr.spoutnik87.musicbot_rest.reader

data class UserUpdateReader(
        var nickname: String?,
        var firstname: String?,
        var lastname: String?
) {
}