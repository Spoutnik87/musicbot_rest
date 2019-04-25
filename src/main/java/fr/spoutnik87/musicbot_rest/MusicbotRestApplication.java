package fr.spoutnik87.musicbot_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class MusicbotRestApplication {

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SpringApplicationContext springApplicationContext() {
    return new SpringApplicationContext();
  }

  @Bean(name = "AppConfig")
  public AppConfig appConfig() {
    return new AppConfig();
  }

  public static void main(String[] args) {
    SpringApplication.run(MusicbotRestApplication.class, args);
  }
}
