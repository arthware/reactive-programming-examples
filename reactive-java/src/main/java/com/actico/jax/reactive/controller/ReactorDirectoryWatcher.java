package com.actico.jax.reactive.controller;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.actico.jax.dirwatcher.DirectoryEvent;
import com.actico.jax.dirwatcher.DirectoryWatcher;
import com.actico.jax.dirwatcher.DirectoryWatcherCallback;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ReactorDirectoryWatcher
{

   static final Logger LOG = LoggerFactory.getLogger(ReactorDirectoryWatcher.class);

   public static Flux<DirectoryEvent> observeSimple(Path path)
   {
      Flux<DirectoryEvent> directoryFlux = Flux.create(emitter -> {
         DirectoryWatcherCallback callback = new DirectoryWatcherCallback()
         {
            @Override
            public void event(DirectoryEvent e)
            {
               if (!emitter.isCancelled())
               {
                  emitter.next(e);
               }
            }

            @Override
            public void finish()
            {
               emitter.complete();
            }

            @Override
            public void error(Throwable e)
            {
               emitter.error(e);
            }

         };
         LOG.info("created directory watcher for path " + path);
         DirectoryWatcher watcher = new DirectoryWatcher(path, callback);
         watcher.watch();
      });
      return directoryFlux;
   }

   public static Flux<DirectoryEvent> observe(Path path)
   {
      Flux<DirectoryEvent> directoryFlux = Flux.create(emitter -> {
         DirectoryWatcherCallback callback = new DirectoryWatcherCallback()
         {
            @Override
            public void event(DirectoryEvent e)
            {
               if (!emitter.isCancelled())
               {
                  emitter.next(e);
               }
            }

            @Override
            public void finish()
            {
               emitter.complete();
            }

            @Override
            public void error(Throwable e)
            {
               emitter.error(e);
            }

         };
         LOG.info("created directory watcher for path " + path);
         DirectoryWatcher watcher = new DirectoryWatcher(path, callback);
         watcher.watch();
      });
      return directoryFlux.subscribeOn(Schedulers.newElastic("elastic-io")).publish().refCount();
   }
}
