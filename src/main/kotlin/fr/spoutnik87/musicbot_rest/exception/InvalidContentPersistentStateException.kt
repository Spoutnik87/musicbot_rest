package fr.spoutnik87.musicbot_rest.exception

class InvalidContentPersistentStateException(contentId: Long)
    : InvalidPersistentStateException("The content $contentId is in an invalid state.")