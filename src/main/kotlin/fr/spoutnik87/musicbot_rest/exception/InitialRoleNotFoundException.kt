package fr.spoutnik87.musicbot_rest.exception

class InitialRoleNotFoundException(roleName: String)
    : Exception("The following Role was not found : $roleName")