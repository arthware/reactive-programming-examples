package com.actico.jax.reactive.metrics;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(value = "/mousetracker")
@CrossOrigin("*")
public class MouseTrackerRestMvcController
{
   private static final Logger LOG = LoggerFactory.getLogger(MouseTrackerRestMvcController.class);

   Map<String, List<Point>> points = new ConcurrentHashMap<>();

   @PostMapping(value = "/{user}")
   public ResponseEntity<String> track(@PathVariable String user, HttpRequest request)
   {
      List<Point> userPoints = points.get(user);
      if (userPoints == null)
      {
         points.put(user, new LinkedList<>());
         URI uri = UriComponentsBuilder.fromHttpRequest(request).build().toUri();
         return ResponseEntity.created(uri).build();
      }
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
   }

   @PutMapping(value = "/{user}")
   public ResponseEntity<String> track(
      @PathVariable String user, @RequestBody Point point)
   {
      try
      {
         Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      LOG.info("Adding {} for user {}", point, user);
      List<Point> clientPoints = points.get(user);
      if (clientPoints == null)
      {
         clientPoints = new LinkedList<>();
         points.put(user, clientPoints);
      }
      clientPoints.add(point);
      return ResponseEntity.ok("Ok");
   }

   @GetMapping(value = "/{user}", produces = "application/json")
   public ResponseEntity<Points> getPoints(@PathVariable String user)
   {
      List<Point> userPoints = points.get(user);
      if (userPoints != null)
      {
         return ResponseEntity.ok(new Points(userPoints));
      }
      return new ResponseEntity<>(new Points(), HttpStatus.NOT_FOUND);
   }


}
