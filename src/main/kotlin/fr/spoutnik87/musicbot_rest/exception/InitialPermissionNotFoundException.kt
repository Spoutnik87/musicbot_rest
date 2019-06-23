package fr.spoutnik87.musicbot_rest.exception

class InitialPermissionNotFoundException(permissionValue: String)
    : Exception("The following Permission was not found : $permissionValue")