package com.actico.jax.reactive.reactor;

import java.nio.file.Paths;

import org.junit.Test;

import com.actico.jax.dirwatcher.DirectoryEvent;
import com.actico.jax.reactive.controller.ReactorDirectoryWatcher;
import com.actico.jax.reactive.metrics.LoggingSubscriber;

import reactor.core.publisher.Flux;

public class DirectoryWatcherTest
{
   @Test
   public void testDirectoryWatcher() throws Exception
   {
      Flux<DirectoryEvent> flux = ReactorDirectoryWatcher.observe(Paths.get("target"));

      flux.subscribe(new LoggingSubscriber<>("fs-1"));
      Thread thread = new Thread(() -> {
         try
         {
            Thread.sleep(10000);
            System.out.println("here we go!");
            flux.subscribe(new LoggingSubscriber<>("fs-2"));
         }
         catch (InterruptedException e)
         {
         }
      });

      thread.run();
      //flux.connect();
      System.out.println("here");

      Thread.sleep(100000);

   }


}
