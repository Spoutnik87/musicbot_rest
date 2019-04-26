package fr.spoutnik87.musicbot_rest.util;

import fr.spoutnik87.musicbot_rest.SpringApplicationContext;
import org.springframework.context.annotation.Bean;

public class SpringApplicationContextTestConfig {

    @Bean
    public SpringApplicationContext springApplicationContext() {
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        return springApplicationContext;
    }
}
