package fr.spoutnik87.musicbot_rest;

import org.springframework.stereotype.Component;

@Component
public class UUID {

    public String v4() {
        return java.util.UUID.randomUUID().toString();
    }
}
