package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateReader {
  private String nickname;

  private String firstname;

  private String lastname;
}
