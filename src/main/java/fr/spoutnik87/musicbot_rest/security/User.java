package fr.spoutnik87.musicbot_rest.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class User {
  @NonNull
  private String email;
  @NonNull
  private String password;
}
