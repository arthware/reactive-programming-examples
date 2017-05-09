package com.actico.jax.reactive.rxjava2;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.actico.jax.reactive.metrics.Metrics;
import com.actico.jax.reactive.metrics.Metrics.Average;
import com.actico.jax.reactive.metrics.Metrics.Metric;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

public class RxJava2MetricsExampleBuffer
{
   private static final Average EMPTY_DEFAULT = Average.empty("empty");

   @Test
   public void metricsMovingAverageExample() throws Exception
   {
      Observable<Metric> metricStream = new Metrics().getMetricsStream();

      Observable<GroupedObservable<String, Metric>> groupMetric =
         metricStream.groupBy(Metric::getName);

      Observable<Average> movingAverageStream = groupMetric.flatMap(calculateMovingAvg2());

      // groupMetric.flatMap(calculateMovingAvg(5))
      movingAverageStream.blockingSubscribe(movingAvg -> System.out.println(movingAvg));


      //Thread.sleep(10000);
   }

   @SuppressWarnings("unchecked")
   private Function<Observable<Metric>, ObservableSource<Average>> calculateMovingAvg2()
   {
      return metricStream -> {
         String metricName = ((GroupedObservable<String, Metric>) metricStream).getKey();
         return metricStream.buffer(50).map(bucket -> {
            Average movingAvg = Average.empty(metricName);
            for (Metric m : bucket)
            {
               movingAvg.add(m);
            }
            return movingAvg;
         });
      };
   }

   /**
    * A function that transforms a Metric stream into a moving average stream. 
    * 
    * @return the transforming function
    */
   private Function<Observable<Metric>, ObservableSource<Average>>
      calculateMovingAvg(int avgWindowSize)
   {
      String defaultKey = "avg";
      return metricStream -> {
         final String key;
         if (metricStream instanceof GroupedObservable<?, ?>)
         {
            key = Objects.toString(((GroupedObservable<?, ?>) metricStream).getKey());
         }
         else
         {
            key = defaultKey;
         }
         return metricStream.buffer(50).flatMap(bucket -> {
            Average movingAvg = Average.empty(key);
            for (Metric m : bucket)
            {
               movingAvg.add(m);
            }
            return Observable.just(movingAvg);
         });

      };
   }

   AtomicInteger windowIdx = new AtomicInteger(1);

   private Function<List<Metric>, ObservableSource<Average>> convertToAverage(final String metricKey)
   {
      return new Function<List<Metric>, ObservableSource<Average>>()
      {
         @Override
         public ObservableSource<Average> apply(List<Metric> bucket) throws Exception
         {
            String name = metricKey + " window[" + windowIdx.getAndIncrement() + "]";
            Average avg = Average.empty(name);
            for (Metric m : bucket)
            {
               avg.add(m);
            }
            /*MovingAvg bucket =
               t.stream()
                  .collect(() -> MovingAvg.empty(metricKey), MovingAvg::capture, MovingAvg::combine);*/
            return Observable.just(avg);
         }
      };
   }


}
