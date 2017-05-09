package com.actico.jax.reactive.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Point
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
