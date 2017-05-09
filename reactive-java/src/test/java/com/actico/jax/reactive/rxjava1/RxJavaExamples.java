package com.actico.jax.reactive.rxjava1;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.actico.jax.reactive.metrics.Point;

import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@SuppressWarnings("boxing")
public class RxJavaExamples
{
   static final Logger LOG = LoggerFactory.getLogger(RxJavaExamples.class);

   @Test
   public void pullModel()
   {

   }

   @Test
   public void imperativeVsDeclarative()
   {

      int maxResults = 4;

      // Good ol' imperative code style ...
      System.out.println("-- Imperative Code --");
      List<BigDecimal> bigDecimalEven = new LinkedList<>();
      for (int i = 0; i < 20; i++)
      {
         int modResult = i % 2;
         if (modResult == 0)
         {
            if (i > 0)
            {
               bigDecimalEven.add(new BigDecimal(i));
               System.out.println("inside imperative codeblock ... " + i);
               if (bigDecimalEven.size() == maxResults)
               {
                  break;
               }
            }
         }
      }
      System.out.println("-- print imperative results --");
      for (BigDecimal number : bigDecimalEven)
      {
         print(number);
      }

      System.out.println("--  JDK 8 functional code style --");
      Stream<BigDecimal> jdk8Stream = IntStream.range(0, 20)
         .filter(i -> i % 2 == 0)
         .skip(1)
         .limit(maxResults)
         .mapToObj(number -> {
            System.out.println("inside jdk8 stream: " + number);
            return BigDecimal.valueOf(number);
         });
      System.out.println("-- print jdk8 stream results --");
      jdk8Stream.forEach(number -> print(number));


      System.out.println("--  RxJava reactive code style --");
      // RxJava reactive style ...
      Observable<BigDecimal> observableStream = Observable.range(0, 20)
         .filter(n -> n % 2 == 0)
         .skip(1)
         .take(maxResults)
         .doOnNext(
            number -> System.out.println("inside reactive pipeline: " + number))
         .map(BigDecimal::valueOf);

      System.out.println("-- print reactive stream results -- ");
      observableStream.forEach(number -> print(number));

   }


   @Test
   public void observableAndObserver()
   {

      Observable<Integer> numberStream =
         Observable.from(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

      numberStream.subscribe(new rx.Observer<Number>()
      {
         @Override
         public void onCompleted()
         {
            LOG.info("onCompleted()");
         }

         @Override
         public void onError(Throwable e)
         {
            LOG.error("onError()", e);
         }

         @Override
         public void onNext(Number p)
         {
            LOG.info("onNext(): {}", p);
         }
      });
   }

   @Test
   public void observableObserverError()
   {
      Iterable<Long> numbers = new Iterable<Long>()
      {
         @Override
         public Iterator<Long> iterator()
         {
            return new Iterator<Long>()
            {
               long val = 1;

               @Override
               public boolean hasNext()
               {
                  return val < 10;
               }

               @Override
               public Long next()
               {
                  return val++;
               }
            };
         }
      };
      Observable<Long> numberStream = Observable.from(numbers);
      numberStream.subscribe(new rx.Observer<Long>()
      {
         @Override
         public void onCompleted()
         {
            LOG.info("onCompleted()");
         }

         @Override
         public void onNext(Long p)
         {
            LOG.info("onNext(): {}", p);
         }

         @Override
         public void onError(Throwable e)
         {
            LOG.error("onError(): {}:{}", e.getClass().getSimpleName(), e.getMessage());
         }
      });
   }

   @Test
   public void operator()
   {

      Observable<Integer> numberStream =
         Observable.from(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

      Observable<BigDecimal> bigDecimalEven =
         numberStream
            .filter(n -> n % 2 == 0)
            .skip(1)
            .take(2)
            .map(BigDecimal::valueOf);


      bigDecimalEven.subscribe(new rx.Subscriber<Number>()
      {
         @Override
         public void onCompleted()
         {
            LOG.info("onCompleted()");
         }

         @Override
         public void onError(Throwable e)
         {
            LOG.error("onError()", e);
         }

         @Override
         public void onNext(Number p)
         {
            LOG.info("onNext(): {}", p);
         }
      });
   }

   @Test
   public void testSingle()
   {
      Observable<String> singleItemStream = Single.just("JAX").toObservable();

      Observable<String> concatWith =
         Single.just("Hello ")
            .concatWith(Single.just("JAX 2017").delay(1, SECONDS));

      concatWith.toBlocking().forEach(System.out::print);

      //httpGetSingle("https://jax.de/programm/", String.class).subscribe(new SysoutSubscriber<String>());
   }

   @Test
   public void testCompletable()
   {
      Completable completable = Completable.fromAction(() -> expensiveDatabaseOperation());
      completable.subscribe(new LoggingSub<Void>());

      completable.toSingle(() -> "Default");
      completable.toObservable();
   }

   private void expensiveDatabaseOperation()
   {

   }


   static <T> Single<T> httpGetSingle(String url, Class<T> resultType)
   {
      RestTemplate http = new RestTemplate();
      return Single.fromCallable(() -> {
         return http.getForEntity(url, resultType).getBody();
      });
   }

   @Test
   public void simpleStreamExample() throws Exception
   {


      Observable<Point> pointStream =
         Observable.from(asList(new Point(1, 1), new Point(2, 2)));
      Observable<Long> intervalStream = Observable.interval(1, TimeUnit.SECONDS);

      Observable<Point> periodicPointObservable =
         Observable.zip(pointStream, intervalStream, (p, i) -> p);

      periodicPointObservable.subscribe(new Observer<Point>()
      {
         @Override
         public void onCompleted()
         {
            LOG.info("onCompleted()");
         }

         @Override
         public void onError(Throwable e)
         {
            LOG.error("onError()", e);
         }

         @Override
         public void onNext(Point p)
         {
            LOG.info("onNext(): {}", p);
         }
      });

      TestSubscriber<String> testSubscriber = TestSubscriber.create();
      Subscription subscription =
         periodicPointObservable.map(p -> p.toString()).subscribe(testSubscriber);

      testSubscriber.awaitTerminalEvent();
      testSubscriber.assertValues("Point [x=1.0, y=1.0]", "Point [x=2.0, y=2.0]");
      testSubscriber.assertCompleted();
      subscription.unsubscribe();
   }

   @Test
   public void pullBased()
   {
      Stream<Integer> list = asList(1, 1, 2, 3, 5, 8, 13, 21).stream();
      list.forEach(e -> System.out.println(e));
   }


   @Test
   public void postValuesToServerWihtRetryLogic() throws Exception
   {
      //Create an Observable emitting points from an array.
      Observable<Point> points = Observable
         .from(Arrays.asList(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4)));

      //Zip with an 'interval' Observable to force emission of items every n milliseconds
      Observable<Point> valueEmitter = Observable.zip(points,
         Observable.interval(500, TimeUnit.MILLISECONDS), (item, intervalValue) -> item);

      // Subscribe to the point emitter to post emitted points asynchronously to a server
      valueEmitter.subscribe(point -> {

         Observable<String> postToServer =
            httpPostObservable("http://localhost:8000/rest/sink", point, String.class);

         //Try posting to server and retry if an error occurs.  
         Observable<String> postWithRetry = postToServer.retryWhen(errorOccurs -> {
            AtomicInteger retries = new AtomicInteger(0);
            return errorOccurs.flatMap(exception -> {
               int currentRetry = retries.incrementAndGet();
               LOG.info("Handling Exception {}. Current retry: {}", exception.getMessage(),
                  currentRetry);
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
         postWithRetry.toBlocking()
            .subscribe(serverResult -> LOG.info("SUCCESS! Server Result: {}", serverResult),
               exception -> LOG.error("Server response: {}", exception.getMessage(), exception));
      });


      //Only for demonstration purposes
      Thread.sleep(30000);
   }

   @Test
   public void postValuesToServerWihtRetryLogicUnderstandable() throws Exception
   {
      //Create an Observable emitting points from an array.
      Observable<Point> points = Observable
         .from(Arrays.asList(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4)));

      //Zip with an 'interval' Observable to force emission of items every n milliseconds
      Observable<Point> valueEmitter = Observable.zip(points,
         Observable.interval(500, TimeUnit.MILLISECONDS), (item, intervalValue) -> item);

      // Subscribe to the point emitter to post emitted points asynchronously to a server
      valueEmitter.subscribe(
         new Subscriber<Point>()
         {

            @Override
            public void onNext(Point value)
            {

               Observable<String> postToServer =
                  httpPostObservable("http://localhost:8000/rest/sink", value, String.class);

               //Try posting to server and retry if an error occurs.  
               Observable<String> postWithRetry = postToServer.retryWhen(Retry.errorOccurs());


               postWithRetry
                  .subscribe(serverResult -> LOG.info("SUCCESS! Server Result: {}", serverResult),
                     exception -> LOG.error("Server response: {}", exception.getMessage(),
                        exception));
            }


            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {

            }
         });


      //Only for demonstration purposes
      Thread.sleep(30000);

   }

   static class Retry implements Func1<Observable<? extends Throwable>, Observable<?>>
   {

      private final int maxRetries = 5;

      static Retry errorOccurs()
      {
         return new Retry();
      }

      @Override
      public Observable<?> call(Observable<? extends Throwable> t)
      {

         return t.flatMap(retryOnError());
      }

      private RetryOrError retryOnError()
      {
         return new RetryOrError();
      }

      private final class RetryOrError implements Func1<Throwable, Observable<?>>
      {
         private final AtomicInteger currentRetry = new AtomicInteger(0);

         @Override
         public Observable<?> call(Throwable t)
         {
            if (currentRetry.getAndIncrement() < maxRetries)
            {
               return Observable.timer(currentRetry.get() * 2, TimeUnit.SECONDS);
            }
            return Observable.error(t);
         }
      }
   }


   @Test
   public void flatMapExample()
   {
      Observable<Void> observable = null;

      Observable<Point> points = Observable
         .just(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4));
      points.doOnNext(p -> System.out.println("Stream val: " + p));

      points.flatMap(flatMapPoints())
         .subscribe(p -> System.out.println("" + p + " " + p.getClass().getSimpleName()));
   }

   Func1<Point, Observable<?>> flatMapPoints()
   {
      return new Func1<Point, Observable<?>>()
      {

         @Override
         public Observable<?> call(Point point)
         {
            return Observable.just(point.getxCoord() + "");
         }
      };
   }

   /**
    * This is not the correct way of doing this. Don't do this at home.
    */
   static <T> Observable<T> httpPostObservable(String url, Object payload, Class<T> resultType)
   {

      return Observable.unsafeCreate(obs -> {
         RestTemplate http = new RestTemplate();
         try
         {
            LOG.warn("This is called for every new subscriber. Probably not really what we want.");
            T result = http.postForObject(url, payload, resultType);
            if (!obs.isUnsubscribed())
            {
               obs.onNext(result);
               obs.onCompleted();
            }
         }
         catch (Throwable e)
         {
            obs.onError(e);
         }
      });
   }

   @Test
   public void postValuesToServerWihtRetryLogicAndOptionalBuffer() throws Exception
   {
      //Create an Observable emitting points from an array.
      Observable<Point> points = Observable
         .from(Arrays.asList(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4)));

      //Zip with an 'interval' Observable to force emission of items every n milliseconds
      Observable<Point> valueEmitter = Observable.zip(points,
         Observable.interval(480, TimeUnit.MILLISECONDS), (item, intervalValue) -> item);

      // Subscribe to the point emitter to post emitted points asynchronously to a server
      valueEmitter
         .buffer(1000, TimeUnit.MILLISECONDS)
         .subscribeOn(Schedulers.io())
         .subscribe(point -> {

            Observable<String> postToServer =
               httpPostObservable("http://localhost:8000/rest/sink", point, String.class)
                  .observeOn(Schedulers.io());
            //Observable<List<String>> postWithRetryAndBuffer = postToServer.buffer(3);

            //Try posting to server and retry if an error occurs.  
            Observable<String> postWithRetry =
               postToServer.retryWhen(exceptionsOccurUntilMaxAttempts(5));


            postWithRetry.subscribe(
               serverResult -> LOG.info("SUCCESS! Server Result: {}", serverResult),
               exception -> LOG.error("Got Error Response from Server: {}", exception.getMessage(),
                  exception),
               () -> LOG.info("Complete"));

         });

      //Only for demonstration purposes
      Thread.sleep(60000);
   }


   private Func1<Observable<? extends Throwable>, Observable<?>>
      exceptionsOccurUntilMaxAttempts(int i)
   {
      return null;
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


   void print(BigDecimal bigDecimal)
   {
      System.out.println(bigDecimal);
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
