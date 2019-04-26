package fr.spoutnik87.musicbot_rest;

public abstract class UUID {

    public static String v4() {
        return java.util.UUID.randomUUID().toString();
    }
}
