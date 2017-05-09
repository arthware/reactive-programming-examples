package com.actico.jax.reactive.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

public class Metrics
{

   public Observable<Metric> getMetricsStream()
   {
      Observable<Metric> range1 = Observable.intervalRange(0, 1000, 0, 10, TimeUnit.MILLISECONDS)
         .map(number -> Metric.random("MetricA", number.intValue() + 1));
      Observable<Metric> range2 = Observable.intervalRange(0, 1000, 2000, 10, TimeUnit.MILLISECONDS)
         .map(number -> Metric.random("MetricB", (number.intValue() + 1) * 10));
      final Observable<Metric> mergedMetrics = range1.mergeWith(range2);
      return mergedMetrics;

      /* Stream<Metric> metricsA = IntStream.range(1, 2000).mapToObj(i -> Metric.random("MetricA"));
      Stream<Metric> metricsB = IntStream.range(1, 1000).mapToObj(i -> Metric.random("MetricB"));
      
      Stream<Metric> metrics = Stream.concat(metricsA, metricsB);
      return metrics;*/
   }

   public ConnectableFlux<Metric> getMetricsFlux()
   {
      Flux<Metric> range1 = Flux.interval(Duration.ofMillis(10), Duration.ofSeconds(1)).take(10)
         .map(number -> Metric.random("MetricA", number.intValue() + 1));
      Flux<Metric> range2 = Flux.interval(Duration.ofSeconds(2), Duration.ofSeconds(1)).take(10)
         .map(number -> Metric.random("MetricB", (number.intValue() + 1) * 10));
      final Flux<Metric> mergedMetrics = range1.mergeWith(range2);
      //mergedMetrics.doOnEach(this::printSignal);
      //mergedMetrics.subscribe(new LoggingSubscriber<Metric>("metricStream"));
      return mergedMetrics.publish();

      /* Stream<Metric> metricsA = IntStream.range(1, 2000).mapToObj(i -> Metric.random("MetricA"));
      Stream<Metric> metricsB = IntStream.range(1, 1000).mapToObj(i -> Metric.random("MetricB"));
      
      Stream<Metric> metrics = Stream.concat(metricsA, metricsB);
      return metrics;*/
   }


   public void printSignal(Signal<Metric> signal)
   {
      System.out.println(signal);
   }


   public Publisher<Metric> getReactiveStreamsMetricStream()
   {
      Observable<Metric> range1 = Observable.intervalRange(0, 1000, 0, 10, TimeUnit.MILLISECONDS)
         .map(number -> Metric.random("MetricA", number.intValue() + 1));
      Observable<Metric> range2 = Observable.intervalRange(0, 1000, 2000, 10, TimeUnit.MILLISECONDS)
         .map(number -> Metric.random("MetricB", (number.intValue() + 1) * 10));
      final Observable<Metric> mergedMetrics = range1.mergeWith(range2);
      return mergedMetrics.toFlowable(BackpressureStrategy.BUFFER);
   }


   /**
    * Immutable metric representation with a name and unit to identify the metric together with a value. 
    * 
    * @author Arthur Hupka
    */
   public static final class Metric
   {

      private final int value;
      private final String metricName;
      private final String unit;

      public Metric(String metricName, int durationMs, String unit)
      {
         super();
         this.value = durationMs;
         this.metricName = metricName;
         this.unit = unit;
      }

      public static Metric random(String name, int upper)
      {
         return new Metric(name, new Random().nextInt(upper), "ms");
      }

      @Override
      public String toString()
      {
         return metricName + " " + value;
      }

      public int getValue()
      {
         return value;
      }

      public String getUnit()
      {
         return unit;
      }

      public String getName()
      {
         return metricName;
      }

   }


   /**
    * Mutable object to calculate moving average
    * 
    * @author Arthur Hupka
    *
    */
   public static class Average
   {
      private BigDecimal total = new BigDecimal(0);
      private final AtomicInteger dataPoints;
      private final String name;
      private String unit = "";

      public Average(String name, int total, int dataPoints)
      {
         this(name, new BigDecimal(total), dataPoints);
      }

      public Average(String name, BigDecimal total, int dataPoints)
      {
         super();
         this.name = name;
         this.total = total;
         this.dataPoints = new AtomicInteger(0);
      }

      public Average add(Metric metric)
      {
         this.total = this.total.add(BigDecimal.valueOf(metric.getValue()));
         this.unit = metric.getUnit();
         this.dataPoints.getAndIncrement();
         return this;
      }


      public Average capture(int n, String unit)
      {
         this.total = this.total.add(BigDecimal.valueOf(n));
         this.dataPoints.getAndIncrement();
         this.unit = unit;
         return this;
      }

      public static Average empty(String name)
      {
         return new Average(name, 0, 0);
      }

      BigDecimal getAvg()
      {
         if (this.dataPoints.get() == 0)
         {
            return BigDecimal.ZERO;
         }
         return total.divide(new BigDecimal(dataPoints.get()), 2, RoundingMode.HALF_UP);
      }

      @Override
      public String toString()
      {
         return name + " Avg: " + getAvg() + this.unit + " / " + this.dataPoints.get()
            + " data points";
      }

      Average combine(Average other)
      {
         throw new IllegalStateException("Not implemtend");
      }
   }


}
