package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class ServerUpdateReader {
  @NonNull private String name;
}
