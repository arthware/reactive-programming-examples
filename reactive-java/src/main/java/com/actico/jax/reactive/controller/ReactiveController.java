package com.actico.jax.reactive.controller;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.actico.jax.dirwatcher.DirectoryEvent;
import com.actico.jax.reactive.metrics.Metrics;
import com.actico.jax.reactive.metrics.Metrics.Metric;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ReactiveController
{
   ConnectableFlux<Metric> metrics;

   Flux<DirectoryEvent> directoryFlux;
   Flux<DirectoryEvent> directoryFluxSimple;

   @PostConstruct
   void init()
   {
      this.metrics = new Metrics().getMetricsFlux();
      this.directoryFlux = ReactorDirectoryWatcher.observe(Paths.get("target"));
      this.directoryFluxSimple = ReactorDirectoryWatcher.observeSimple(Paths.get("target"));

   }


   @GetMapping(value = "metrics.stream", produces = "application/stream+json")
   public Publisher<Metric> metricStream()
   {
      metrics.connect();
      return metrics;
   }

   @GetMapping(value = "directory.stream", produces = "application/stream+json")
   public Publisher<DirectoryEvent> directoryStream()
   {
      return directoryFluxSimple;
   }

   @GetMapping(value = "metrics/simple", produces = "application/json")
   public Publisher<List<Metric>> metrics()
   {
      ConnectableFlux<Metric> conMetrics = metrics;
      conMetrics.connect();

      return conMetrics.window(Duration.ofSeconds(2)).flatMap(blah());
   }


   private Function<Flux<Metric>, Mono<List<Metric>>> blah()
   {
      return input -> input.collect(Collectors.toList());
   }
}
