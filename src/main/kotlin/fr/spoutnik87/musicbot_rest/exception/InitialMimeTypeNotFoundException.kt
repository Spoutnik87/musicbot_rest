package fr.spoutnik87.musicbot_rest.exception

class InitialMimeTypeNotFoundException(mimeTypeValue: String)
    : Exception("The following MimeType was not found : $mimeTypeValue")