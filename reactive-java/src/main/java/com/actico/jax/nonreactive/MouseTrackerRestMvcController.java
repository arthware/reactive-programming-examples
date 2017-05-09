package com.actico.jax.nonreactive;

import java.net.URI;
import java.util.Collections;
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

   public static class Points
   {
      List<Point> data = Collections.emptyList();


      public Points()
      {
         super();
      }

      public Points(List<Point> data)
      {
         super();
         this.data = data;
      }

      public List<Point> getData()
      {
         return data;
      }

      public void setData(List<Point> data)
      {
         this.data = data;
      }

   }

   public static class Point
   {
      final double xCoord;
      final double yCoord;
      final long ts;

      @JsonCreator
      public Point(@JsonProperty("xCoord") double xCoord, @JsonProperty("yCoord") double yCoord)
      {
         super();
         this.xCoord = xCoord;
         this.yCoord = yCoord;
         ts = System.currentTimeMillis();
      }

      @Override
      public String toString()
      {
         return "Point [x=" + xCoord + ", y=" + yCoord + "]";
      }

      public double getxCoord()
      {
         return xCoord;
      }

      public double getyCoord()
      {
         return yCoord;
      }

      public long getTs()
      {
         return ts;
      }

   }

}
