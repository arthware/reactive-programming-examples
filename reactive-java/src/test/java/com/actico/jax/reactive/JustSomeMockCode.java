package com.actico.jax.reactive;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;

/**
 * Random code examples 
 * 
 * @author Arthur Hupka
 *
 */
public class JustSomeMockCode
{
   public void actionPerformed(ActionEvent ae)
   {
      JButton o = (JButton) ae.getSource();
      String value = o.getText();
      if ("Apocalypse Now!".equals(value))
      {
         launchNuclearWeapons();
      }
   }

   private void launchNuclearWeapons()
   {

   }

   @SuppressWarnings({ "unused", "boxing" })
   public void iterator()
   {

      List<Integer> numbers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
      Iterator<Integer> iterator = numbers.iterator();
      while (iterator.hasNext())
      {
         Integer num = iterator.next();
         //do something with num
      }

   }


}
