package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserUpdateReader {
  @NonNull private String nickname;

  @NonNull private String firstname;

  @NonNull private String lastname;
}
