package com.actico.jax.dirwatcher;

import java.nio.file.Path;

public class DirectoryEvent {
   private final Path name;
   private final Path path;
   private final boolean directory;
   private final long timestamp;
   private final Kind kind;

   public static enum Kind {
      CREATE,
      MODIFY,
      DELETE,
      UNKNOWN;
   }

   public DirectoryEvent(Path name, Path path, boolean directory, Kind kind) {
      super();
      this.name = name;
      this.path = path;
      this.directory = directory;
      this.kind = kind;
      this.timestamp = System.currentTimeMillis();
   }

   public Path getName() {
      return name;
   }

   public Path getPath() {
      return path;
   }

   public boolean isDirectory() {
      return directory;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public Kind getKind() {
      return kind;
   }

   @Override
   public String toString() {
      return "DirectoryEvent [" + kind + ", " + path + ", directory=" + directory + "]";
   }

}
