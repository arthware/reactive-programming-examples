package com.actico.jax.reactive.metrics;

import java.util.Collections;
import java.util.List;

public class Points
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

