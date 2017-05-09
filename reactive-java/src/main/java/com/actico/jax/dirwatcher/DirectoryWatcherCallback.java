package com.actico.jax.dirwatcher;


/**
 * Non-reactive callback interface to watch directories
 * 
 * @author Arthur Hupka
 *
 */
public interface DirectoryWatcherCallback
{

   void event(DirectoryEvent e);

   void finish();

   void error(Throwable e);

}
