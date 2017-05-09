package com.actico.jax.reactive.rxjava1;

import java.nio.file.Path;

import com.actico.jax.dirwatcher.DirectoryEvent;
import com.actico.jax.dirwatcher.DirectoryWatcher;
import com.actico.jax.dirwatcher.DirectoryWatcherCallback;

import rx.Observable;
import rx.Subscriber;

public class RxJava1DirectoryWatcher
{

   public static Observable<DirectoryEvent> observe(Path path)
   {
      Observable<DirectoryEvent> pathObservable =
         Observable.unsafeCreate(new Observable.OnSubscribe<DirectoryEvent>()
         {

            @Override
            public void call(Subscriber<? super DirectoryEvent> subscriber)
            {
               System.out.println("Subscribe : " + subscriber);
               DirectoryWatcherCallback directoryObserver = new DirectoryWatcherCallback()
               {
                  @Override
                  public void event(DirectoryEvent e)
                  {
                     System.out.println("On next: " + Thread.currentThread().getName());
                     if (!subscriber.isUnsubscribed())
                     {
                        subscriber.onNext(e);
                     }
                  }

                  @Override
                  public void error(Throwable e)
                  {
                     subscriber.onError(e);
                  }

                  @Override
                  public void finish()
                  {
                     subscriber.onCompleted();
                  }

               };
               DirectoryWatcher directoryWatcher = new DirectoryWatcher(path, directoryObserver);
               directoryWatcher.watch();
            }

         });
      return pathObservable;
   }
}
