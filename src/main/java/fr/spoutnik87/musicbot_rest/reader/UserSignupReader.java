package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class UserSignupReader {

  @NonNull private String email;

  @NonNull private String nickname;

  @NonNull private String firstname;

  @NonNull private String lastname;

  @NonNull private String password;
}
