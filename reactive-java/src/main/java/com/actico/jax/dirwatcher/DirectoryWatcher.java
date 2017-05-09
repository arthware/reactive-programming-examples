package com.actico.jax.dirwatcher;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import com.actico.jax.dirwatcher.DirectoryEvent.Kind;

/**
 * Simple NIO API Directory Watcher 
 * 
 * @author Arthur Hupka
 *
 */
public class DirectoryWatcher implements Runnable
{
   private final WatchService watcher;
   private DirectoryWatcherCallback observer;
   private Path path;
   boolean doWatch = true;
   Map<WatchKey, Path> keys = new ConcurrentHashMap<>();

   public DirectoryWatcher(Path path, DirectoryWatcherCallback observer)
   {
      this.path = path;
      final FileSystem fileSystem = path.getFileSystem();
      this.observer = observer;
      try
      {
         watcher = fileSystem.newWatchService();
         registerAll(path);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   private WatchKey watchDir(Path path)
   {
      try
      {
         WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
         keys.put(key, path);
         return key;
      }
      catch (IOException e)
      {
         observer.error(e);
         return null;
      }
   }

   private DirectoryWatcher internalWatch()
   {
      do
      {
         WatchKey key = null;
         try
         {
            key = watcher.take();
         }
         catch (InterruptedException iex)
         {
            observer.error(iex);
            doWatch = false;
         }
         catch (ClosedWatchServiceException e)
         {
            doWatch = false;
         }
         if (key != null)
         {
            final Path keyPath = keys.get(key);
            key.pollEvents().stream().map(this::cast).forEach(e -> consume(e, keyPath));
            if (!key.reset())
            {
               //Directory has been deleted.
               keys.remove(key);
               if (keyPath.equals(this.path))
               {
                  doWatch = false;
               }
            }
         }
      }
      while (doWatch == true);
      observer.finish();
      return this;
   }

   private void registerAll(final Path rootDirectory)
   {
      try
      {
         Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>()
         {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir,
               final BasicFileAttributes attrs)
               throws IOException
            {
               watchDir(dir);
               return FileVisitResult.CONTINUE;
            }
         });
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   void consume(WatchEvent<Path> key, Path parent)
   {
      WatchEvent<Path> event = cast(key);
      WatchEvent.Kind<?> kind = event.kind();
      if (kind == OVERFLOW)
      {
         return;
      }
      Path filename = event.context();
      Path child = path.resolve(filename);
      boolean directory = Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS);
      if (directory && kind.equals(StandardWatchEventKinds.ENTRY_CREATE))
      {
         registerAll(child);
      }
      observer.event(new DirectoryEvent(filename, child, directory, mapKind(kind)));
   }

   private Kind mapKind(java.nio.file.WatchEvent.Kind<?> kind)
   {
      if (kind.equals(ENTRY_CREATE))
      {
         return DirectoryEvent.Kind.CREATE;
      }
      else if (kind.equals(ENTRY_DELETE))
      {
         return DirectoryEvent.Kind.DELETE;
      }
      else if (kind.equals(ENTRY_MODIFY))
      {
         return DirectoryEvent.Kind.MODIFY;
      }
      else
      {
         return DirectoryEvent.Kind.UNKNOWN;
      }
   }

   @SuppressWarnings("unchecked")
   private WatchEvent<Path> cast(WatchEvent<?> event)
   {
      return (WatchEvent<Path>) event;
   }

   public DirectoryWatcher watch()
   {
      return internalWatch();
   }

   public DirectoryWatcher watchAsynch()
   {
      ForkJoinPool.commonPool().execute(this);
      return this;
   }

   @Override
   public void run()
   {
      internalWatch();
   }

   public void stop()
   {
      this.doWatch = false;
      try
      {
         this.watcher.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      System.out.println("Stopped");
   }


}
