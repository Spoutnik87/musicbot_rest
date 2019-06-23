package fr.spoutnik87.musicbot_rest.exception

/**
 * Fired when the database is in an invalid state.
 */
abstract class InvalidPersistentStateException(message: String) : Exception(message)