package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.service.threading.ThreadingService;
import com.rackspace.papi.service.threading.impl.ThreadingServiceImpl;
import org.atomnuke.container.service.annotation.NukeService;
import org.atomnuke.service.runtime.AbstractRuntimeService;

/**
 * This is a simple, discoverable adapter for the Repose threading service.
 *
 * @author zinic
 */
@NukeService
public class NukeThreadingService extends AbstractRuntimeService {

   private final ThreadingService service;

   public NukeThreadingService() {
      super(NukeThreadingService.class);

      service = new ThreadingServiceImpl();
   }

   @Override
   public Object instance() {
      return service;
   }
}
