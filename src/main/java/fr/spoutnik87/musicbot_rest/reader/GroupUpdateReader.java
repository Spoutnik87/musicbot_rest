package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class GroupUpdateReader {
  @NonNull private String name;
}
