package com.actico.jax.reactive.rxjava2;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.actico.jax.reactive.metrics.Metrics.Average;
import com.actico.jax.reactive.metrics.Metrics.Metric;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

public class RxJava2MetricsExampleScan {


   private static final Average EMPTY_DEFAULT = Average.empty("empty");

   @Test
   public void metricsMovingAverageExample() throws Exception {
      Observable<Metric> metricStream = createMetricsStream();

      Observable<GroupedObservable<String, Metric>> groupMetric =
         metricStream.groupBy(Metric::getName);

      groupMetric.flatMap(calculateMovingAvg("avg")).subscribeOn(Schedulers.computation())
         .blockingSubscribe(movingAvg -> System.out.println(movingAvg));
      Thread.sleep(10000);
   }


   /**
    * A function that transforms a Metric stream into a moving average stream. 
    * 
    * @param defaultKey the default key in case the key cannot be determined from the observable
    * @return the transforming function
    */
   private Function<Observable<Metric>, ObservableSource<Average>>
      calculateMovingAvg(String defaultKey) {

      return metricStream -> {
         final String key;
         if (metricStream instanceof GroupedObservable<?, ?>) {
            key = Objects.toString(((GroupedObservable<?, ?>) metricStream).getKey());
         }
         else {
            key = defaultKey;
         }
         return metricStream.window(50).flatMap(metricWindow -> movingAvg(key).apply(metricWindow));

      };
   }

   AtomicInteger wIdx = new AtomicInteger(1);

   private Function<Observable<Metric>, ObservableSource<Average>>
      movingAvg(String metricKey) {
      return metricStream -> {
         String name = metricKey + " window[" + wIdx.getAndIncrement() + "]";
         Observable<Average> avg = metricStream.scan(Average.empty(name),
            (movingAvg, metric) -> movingAvg.add(metric));
         avg.last(EMPTY_DEFAULT).doOnSuccess(a -> {
            if (wIdx.get() > 10) {
               throw new IllegalStateException("Uh oh");
            }
         }).toObservable();
         return avg;

      };
   }


   Observable<Metric> createMetricsStream() {
      Observable<Metric> range1 = Observable.intervalRange(0,
         items(1000),
         initialDelay(0),
         emitEvery(10), TimeUnit.MILLISECONDS)
         .map(emittedItemNr -> Metric.random("MetricA", emittedItemNr.intValue() + 1));

      Observable<Metric> range2 = Observable.intervalRange(0,
         items(1000),
         initialDelay(2000),
         emitEvery(10), TimeUnit.MILLISECONDS)
         .map(emittedItemNr -> Metric.random("MetricB", (emittedItemNr.intValue() + 1) * 10));

      final Observable<Metric> mergedMetrics = range1.mergeWith(range2);

      return mergedMetrics;

      /* Stream<Metric> metricsA = IntStream.range(1, 2000).mapToObj(i -> Metric.random("MetricA"));
      Stream<Metric> metricsB = IntStream.range(1, 1000).mapToObj(i -> Metric.random("MetricB"));
      
      Stream<Metric> metrics = Stream.concat(metricsA, metricsB);
      return metrics;*/
   }


   private static long emitEvery(int i) {
      // TODO Auto-generated method stub
      return 0;
   }


   static long initialDelay(int i) {
      return i;
   }


   private static long items(int i) {
      return i;
   }


}
