package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class GroupCreateReader {
  @NonNull private long serverId;
  @NonNull private String name;
}
