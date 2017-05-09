package com.actico.jax.reactive.rxjava2;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.actico.jax.reactive.metrics.LoggingSubscriber;
import com.actico.jax.reactive.metrics.Metrics;
import com.actico.jax.reactive.metrics.Metrics.Average;
import com.actico.jax.reactive.metrics.Metrics.Metric;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

public class RxJava2MetricsExampleBufferTemplate
{
   private final Logger LOG = LoggerFactory.getLogger("LiveHack");

   @Test
   public void metricsMovingAverageExample() throws Exception
   {
      Observable<Metric> metricStream = new Metrics().getMetricsStream();
      //metricStream.blockingSubscribe(new LoggingSubscriber<>());

      Observable<GroupedObservable<String, Metric>> obsGrouped = metricStream.groupBy(Metric::getName);


      Observable<Average> movingAvg = obsGrouped.flatMap(calcMovingAvg());
      movingAvg.blockingSubscribe(new LoggingSubscriber());
      //System.out.println("Yipee, non-blocking!");
      //Thread.sleep(10000);
   }

   private Function<GroupedObservable<String, Metric>, Observable<Average>> calcMovingAvg()
   {
      return new Function<GroupedObservable<String, Metric>, Observable<Metrics.Average>>()
      {
         @Override
         public Observable<Average> apply(GroupedObservable<String, Metric> t) throws Exception
         {
            Observable<Average> avg = t.buffer(50).map(window -> {
               Average a = Average.empty(t.getKey());
               for (Metric m : window)
               {
                  a.add(m);
               }
               return a;
            });
            return avg;
         }

      };
   }
}
