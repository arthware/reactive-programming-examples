package com.actico.jax.reactive.rxjava2;

import java.nio.file.Path;

import com.actico.jax.dirwatcher.DirectoryEvent;
import com.actico.jax.dirwatcher.DirectoryWatcher;
import com.actico.jax.dirwatcher.DirectoryWatcherCallback;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class RxJava2DirectoryWatcher {

   public static Observable<DirectoryEvent> observe(Path path) {
      Observable<DirectoryEvent> pathObservable =
         Observable.create(new ObservableOnSubscribe<DirectoryEvent>() {

            @Override
            public void subscribe(ObservableEmitter<DirectoryEvent> observer) throws Exception {
               DirectoryWatcherCallback directoryObserver = new DirectoryWatcherCallback() {
                  @Override
                  public void event(DirectoryEvent e) {
                     System.out.println("On next: " + Thread.currentThread().getName());
                     if (!observer.isDisposed()) {
                        observer.onNext(e);
                     }
                  }

                  @Override
                  public void error(Throwable e) {
                     observer.onError(e);
                  }

                  @Override
                  public void finish() {
                     observer.onComplete();
                  }

               };
               DirectoryWatcher directoryWatcher = new DirectoryWatcher(path, directoryObserver);
               directoryWatcher.watch();
               observer.setCancellable(() -> directoryWatcher.stop());
            }

         });
      return pathObservable;
   }
}
