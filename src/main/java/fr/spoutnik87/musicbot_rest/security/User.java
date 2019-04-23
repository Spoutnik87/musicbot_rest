package fr.spoutnik87.musicbot_rest.security;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class User {
  @NonNull private String email;
  @NonNull private String password;
}
