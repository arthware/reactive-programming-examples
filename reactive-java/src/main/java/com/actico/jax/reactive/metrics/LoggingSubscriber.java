package com.actico.jax.reactive.metrics;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LoggingSubscriber<T> implements Subscriber<T>, Observer<T>
{
   String name;

   private Subscription s;
   //RxJava 2
   private Disposable disposable;

   public LoggingSubscriber(String name)
   {
      super();
      this.name = name;
   }


   public LoggingSubscriber()
   {
      this("");
   }


   @Override
   public void onComplete()
   {
      System.out.println(log(" onComplete()"));
   }

   @Override
   public void onError(Throwable e)
   {
      System.out.println(log(" onError() "));
      e.printStackTrace();
   }

   @Override
   public void onNext(T t)
   {
      System.out.println(log(" " + t));
   }

   private String log(String message)
   {
      return log(this.name, message);
   }

   public static String log(String prefix, String msg)
   {
      return String.format("%30s", Thread.currentThread().getName()) + " " + String.format("%15s", prefix) + " - " + msg;
   }


   @Override
   public void onSubscribe(Subscription s)
   {
      this.s = s;
      s.request(Long.MAX_VALUE); //Unbounded subscriber
   }

   public final void cancel()
   {
      this.s.cancel();
      if (disposable != null)
      {
         this.disposable.dispose();
      }
   }


   /* RxJava 2 specific - be compatible with */
   @Override
   public void onSubscribe(Disposable disposable)
   {
      this.disposable = disposable;
   }

   public static <T> LoggingSubscriber<T> create(String name)
   {
      return new LoggingSubscriber<>(name);
   }
}
