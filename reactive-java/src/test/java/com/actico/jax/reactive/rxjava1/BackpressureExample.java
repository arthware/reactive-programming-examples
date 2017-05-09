package com.actico.jax.reactive.rxjava1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class BackpressureExample
{
   static final Logger LOG = LoggerFactory.getLogger(RxJavaExamples.class);

   CountDownLatch finish = new CountDownLatch(1);
   AtomicReference<Throwable> error = new AtomicReference<>();

   @Test
   public void simpleBackpressureExample() throws Exception
   {
      PublishSubject<Integer> source = PublishSubject.create();

      source
         //.onBackpressureDrop()
         //.onBackpressureBuffer(100)
         .observeOn(Schedulers.computation())
         .subscribe(item -> consume(item), handleError());

      emitNumbers(source);

      finish.await();
      if (error.get() != null)
      {
         error.get().printStackTrace();
      }

   }

   private void emitNumbers(PublishSubject<Integer> source)
   {
      new Thread(() -> {
         for (int i = 0; i < 1_000; i++)
         {
            if (error.get() != null)
            {
               return;
            }
            if (i < 50)
            {

               sleep(100);
            }
            else
            {
               sleep(5);
            }
            LOG.info("Emit: " + i);
            source.onNext(Integer.valueOf(i));
         }
         finish.countDown();

      }, "number-emitter").start();

   }

   private void sleep(int ms)
   {
      try
      {
         Thread.sleep(ms);
      }
      catch (Exception e)
      {
      }

   }

   private Action1<Throwable> handleError()
   {
      return exception -> {
         error.set(exception);
         LOG.error("Error: ", error.get());
         finish.countDown();
      };
   }


   private Object consume(Integer v)
   {
      try
      {
         LOG.info("Consume: " + v);
         if (v > 50)
         {
            TimeUnit.MILLISECONDS.sleep(v / 100);
         }
         else
         {
            TimeUnit.MILLISECONDS.sleep(100);
         }
         return Void.TYPE;
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
   }

}
