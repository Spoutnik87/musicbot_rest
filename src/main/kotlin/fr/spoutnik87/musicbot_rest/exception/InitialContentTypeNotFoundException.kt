package fr.spoutnik87.musicbot_rest.exception

class InitialContentTypeNotFoundException(contentTypeValue: String)
    : Exception("The following ContentType was not found : $contentTypeValue")