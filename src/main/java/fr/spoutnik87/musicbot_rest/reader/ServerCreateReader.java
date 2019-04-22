package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class ServerCreateReader {

  @NonNull private String name;

  @NonNull private String token;
}
