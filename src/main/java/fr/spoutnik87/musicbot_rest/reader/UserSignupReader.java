package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserSignupReader {

  @NonNull private String email;

  @NonNull private String nickname;

  @NonNull private String firstname;

  @NonNull private String lastname;

  @NonNull private String password;
}
