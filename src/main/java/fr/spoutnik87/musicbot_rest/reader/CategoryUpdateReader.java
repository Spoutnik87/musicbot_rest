package fr.spoutnik87.musicbot_rest.reader;

import lombok.Data;
import lombok.NonNull;

@Data
public class CategoryUpdateReader {
  @NonNull private String name;
}
