package com.actico.jax.reactive.rxjava2;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.actico.jax.reactive.metrics.LoggingSubscriber;
import com.actico.jax.reactive.metrics.Point;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;

@SuppressWarnings("boxing")
public class RxJava2Examples
{
   private final Logger LOG = LoggerFactory.getLogger(RxJava2Examples.class);

   LoggingSubscriber<Object> loggingSubscriber;

   @Before
   public void setup()
   {
      loggingSubscriber = new LoggingSubscriber<>();
   }


   @Test
   public void zipOperatorExample()
   {
      Observable<Point> pointStream = Observable.fromArray(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4));
      Observable<Long> timerStream = Observable.timer(1, TimeUnit.SECONDS).repeat(4);
      Observable<Point> delayedPoints = pointStream.zipWith(timerStream, (point, timer) -> point);
      Observable<List<Point>> buffered = delayedPoints.buffer(2);

      Observable<Point> delayedBufferedPoints = buffered.flatMapIterable(point -> point);


      delayedPoints.subscribe(LoggingSubscriber.create("DelayedPoints"));
      buffered.blockingSubscribe(LoggingSubscriber.create("Buffered"));

   }

   @Test
   public void simpleStreamExample() throws Exception
   {
      @SuppressWarnings("unchecked")
      Observable<List<Point>> points =
         Observable.fromArray(Arrays.asList(new Point(1, 1), new Point(2, 2)));
      Observable<Point> pointStream = points.flatMapIterable(p -> p).delay(1, TimeUnit.SECONDS);

      pointStream.map(p -> p.toString()).blockingSubscribe(p -> System.out.println("p: " + p));
   }

   @Test
   public void pullBased()
   {
      Stream<Integer> list = Arrays.asList(1, 1, 2, 3, 5, 8, 13, 21).stream();
      list.forEach(e -> System.out.println(e));
   }


   @Test
   public void postValuesToServerWihtRetryLogic() throws Exception
   {
      //Create an Observable emitting points from an array.
      Observable<Point> points =
         Observable.fromArray(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4));

      //Zip with an 'interval' Observable to force emission of items every n milliseconds
      Observable<Point> valueEmitter = Observable.zip(points,
         Observable.interval(500, TimeUnit.MILLISECONDS), (item, intervalValue) -> item);

      // Convert each emitted point to a server response

      Observable<String> postToServerStream = valueEmitter.flatMap(point -> {
         Observable<String> postToServer = RxJava2CustomObservable
            .httpPostObservable("http://localhost:8000/rest/sink", point, String.class);

         Observable<String> postToServerWithRetry = postToServer.retryWhen(errorOccurs -> {
            AtomicInteger retries = new AtomicInteger(0);
            return errorOccurs.map(exception -> {
               int currentRetry = retries.incrementAndGet();
               LOG.info("Handling Exception {}. Current retry: {}", exception.getMessage(),
                  currentRetry);
               System.out.println();
               int retryLimit = 5;
               if (currentRetry < retryLimit)
               {
                  // The timeout between each retry is increased by the retry #*2 seconds
                  return Observable.timer(currentRetry * 2, TimeUnit.SECONDS);
               }
               // If retry limit is reached, return error observable to propage the exception
               return Observable.error(exception);
            });
         });

         return postToServerWithRetry;

      });

      postToServerStream
         .subscribe(
            serverResult -> LOG.info("SUCCESS! Server Result: {}", serverResult),
            exception -> LOG.error("Server response: {}", exception.getMessage(), exception));


      //Only for demonstration purposes
      Thread.sleep(30000);
   }

   private Function<? super Observable<Throwable>, ? extends ObservableSource<?>>
      exceptionsOccurUntilMaxAttempts(int maxAttempts)
   {
      return new Function<Observable<Throwable>, ObservableSource<?>>()
      {
         @Override
         public ObservableSource<?> apply(Observable<Throwable> error) throws Exception
         {
            return Observable.range(1, maxAttempts)
               .zipWith(error, (attempt, ex) -> new AttemptWithEx(attempt, ex))
               .flatMap(attemptWithEx -> {
                  if (attemptWithEx.attempt == maxAttempts)
                  {
                     return Observable.error(attemptWithEx.ex);
                  }
                  LOG.warn("Error occured! Current attempt: {} ex message: {}",
                     attemptWithEx.attempt, attemptWithEx.ex.getMessage());
                  int delaySeconds = attemptWithEx.attempt * 5;
                  LOG.warn("Delaying by {} {}", delaySeconds, TimeUnit.SECONDS);
                  Observable<Long> delay = Observable.timer(delaySeconds, TimeUnit.SECONDS);
                  return delay;
               });
         }
      };
   }

   static class AttemptWithEx
   {
      final int attempt;
      final Throwable ex;

      public AttemptWithEx(int attempt, Throwable ex)
      {
         super();
         this.attempt = attempt;
         this.ex = ex;
      }


   }

   Single<String> postToServerSingle(Point item)
   {
      return Single.create(obs -> {
         RestTemplate http = new RestTemplate();
         try
         {
            System.out.println();
            String result =
               http.postForObject("http://localhost:8000/rest/sink", item, String.class);
            obs.onSuccess(result);
         }
         catch (Throwable e)
         {
            obs.onError(e);
         }
      });
   }


}
