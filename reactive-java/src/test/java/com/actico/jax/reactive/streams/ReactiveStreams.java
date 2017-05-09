package com.actico.jax.reactive.streams;


import org.junit.Test;
import org.reactivestreams.Publisher;

import com.actico.jax.reactive.metrics.LoggingSubscriber;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import reactor.core.publisher.Flux;

public class ReactiveStreams
{
   @Test
   public void testInterop()
   {
      Publisher<String> flow = Observable.just("1", "2", "3").toFlowable(BackpressureStrategy.BUFFER);
      Publisher<String> flux = Flux.just("A", "B", "C");

      Publisher<String> fluxResult = Flux.zip(flow, flux, (a, b) -> a + b);
      Publisher<String> flowableResult = Flowable.zip(flow, flux, (a, b) -> b + a);

      fluxResult.subscribe(new LoggingSubscriber<>("flux"));
      flowableResult.subscribe(new LoggingSubscriber<>("flowable"));
   }


}
