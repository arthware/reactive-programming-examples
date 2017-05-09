package com.actico.jax.reactive.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.actico.jax.dirwatcher.DirectoryEvent;
import com.actico.jax.dirwatcher.DirectoryWatcher;
import com.actico.jax.dirwatcher.DirectoryWatcherCallback;
import com.actico.jax.reactive.rxjava2.RxJava2DirectoryWatcher;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FsWatcherTest {
   @Test(timeout = 300000)
   public void testWatch() throws Exception {
      Path testPath = Paths.get("target/test-file-system");
      deleteDir(testPath);
      testPath = Files.createDirectories(testPath);
      CountDownLatch latch = new CountDownLatch(4);
      DirectoryWatcher w = new DirectoryWatcher(testPath, new DirectoryWatcherCallback() {
         @Override
         public void event(DirectoryEvent e) {
            System.out.println("Event: " + e);
            latch.countDown();
         }

         @Override
         public void error(Throwable e) {
            System.out.println("error");
         }

         @Override
         public void finish() {
            System.out.println("completed");
         }
      }).watchAsynch();
      Files.createFile(testPath.resolve("test-file.txt"));
      Path subDirectory = Files.createDirectory(testPath.resolve("sub-dir"));
      Thread.sleep(10); // NOSONAR As we are using the "real" filesystem for testing here. 
      Files.createFile(subDirectory.resolve("test-dir-file.txt"));
      latch.await();
      w.stop();
   }

   @Test(timeout = 300000)
   public void testRxWatch() throws Exception {
      Path testPath = Paths.get("target/test-file-system");
      deleteDir(testPath);
      testPath = Files.createDirectories(testPath);
      Observable<DirectoryEvent> observablePath = RxJava2DirectoryWatcher.observe(testPath);
      Disposable subscription = observablePath
         //.filter(p -> !p.isDirectory())
         .groupBy(f -> f.isDirectory())
         .buffer(1, TimeUnit.SECONDS)
         .subscribeOn(Schedulers.io())
         .subscribe(e -> {
            System.out.println("here: " + Thread.currentThread().getName());
            e.forEach(ev -> ev.subscribe(ab -> System.out.println(ab)));
         });
      Thread.sleep(10); // NOSONAR As we are using the "real" filesystem for testing here. 
      Files.createDirectory(testPath.resolve("sub-dir-2"));
      Files.createFile(testPath.resolve("test-file.txt"));
      Path subDirectory = Files.createDirectory(testPath.resolve("sub-dir"));

      Thread.sleep(10); // NOSONAR As we are using the "real" filesystem for testing here. 
      Files.createFile(subDirectory.resolve("test-dir-file.txt"));
      Thread.sleep(3000);
      //subscription.dispose();
   }

   public void deleteDir(Path rootPath) throws IOException {
      Files.walk(rootPath)
         .sorted(Comparator.reverseOrder())
         .map(Path::toFile)
         .forEach(java.io.File::delete);
   }


}
