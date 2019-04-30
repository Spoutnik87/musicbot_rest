package fr.spoutnik87.musicbot_rest.model

class Views {
    companion object {
        open class Public
        class Private : Public()
    }
}