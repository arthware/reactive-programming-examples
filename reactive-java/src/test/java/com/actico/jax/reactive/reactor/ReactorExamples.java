package com.actico.jax.reactive.reactor;

import java.math.BigDecimal;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

@SuppressWarnings("boxing")
public class ReactorExamples {
   static final Logger LOG = LoggerFactory.getLogger(ReactorExamples.class);

   @Test
   public void operator() {

      Flux<Integer> numberStream = Flux.range(0, 20);

      Flux<BigDecimal> bigDecimalEven =
         numberStream
            .filter(n -> n % 2 == 0)
            .skip(1)
            .take(4)
            .map(BigDecimal::valueOf);


      bigDecimalEven.subscribe(new Subscriber<Number>()
      // Use Reactors BaseSubscriber instead
      {
         Subscription subscription;

         @Override
         public void onComplete() {
            LOG.info("onCompleted()");
         }

         @Override
         public void onError(Throwable e) {
            LOG.error("onError()", e);
         }

         @Override
         public void onNext(Number p) {
            LOG.info("onNext(): {}", p);
            subscription.request(1);
         }

         @Override
         public void onSubscribe(Subscription s) {
            LOG.info("onSubscribe(): {}", s);
            this.subscription = s;
            //s.request(1); //Nothing happens if not called.
            s.request(Long.MAX_VALUE); //Makes subscription.request(1) in onNext() optional
            //Some time in future
            //subscription.cancel();
         }

      });

      bigDecimalEven.subscribe(
         val -> LOG.info("onNext(): {}", val),
         error -> LOG.error("onError() {}", error));

   }

   @Test
   void connectWithCallback() {
   }


}
