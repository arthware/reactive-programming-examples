package com.actico.jax.reactive.rxjava1;

import rx.Subscriber;

public class LoggingSub<T> extends Subscriber<T>
{

   @Override
   public void onCompleted()
   {
      System.out.println(threadName() + " onCompleted()");
   }

   @Override
   public void onError(Throwable e)
   {
      System.out.println(threadName() + " onError() ");
      e.printStackTrace();
   }

   @Override
   public void onNext(T t)
   {
      System.out.println(threadName() + " " + t);
   }

   private String threadName()
   {
      return "[" + Thread.currentThread().getName() + "]";
   }

   public static <T> LoggingSub<T> create()
   {
      return new LoggingSub<>();
   }

}
