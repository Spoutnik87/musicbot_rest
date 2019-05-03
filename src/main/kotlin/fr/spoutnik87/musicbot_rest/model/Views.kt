package fr.spoutnik87.musicbot_rest.model

class Views {
    companion object {
        open class Public
        open class Mixed : Public()
        class Private : Mixed()
    }
}