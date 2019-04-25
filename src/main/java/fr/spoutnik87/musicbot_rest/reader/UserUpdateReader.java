package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class UserUpdateReader {
  @NonNull private String nickname;

  @NonNull private String firstname;

  @NonNull private String lastname;
}
