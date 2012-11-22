package com.rackspace.papi.service.threading.impl;

import com.rackspace.papi.service.threading.ThreadingService;

/**
 *
 * @author zinic
 */
public class ThreadingServiceImpl implements ThreadingService {

   public ThreadingServiceImpl() {
   }

   @Override
   public Thread newThread(Runnable r, String name) {
      return new Thread(r, name);
   }
}
