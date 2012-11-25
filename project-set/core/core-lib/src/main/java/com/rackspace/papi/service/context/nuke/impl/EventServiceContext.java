package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.util.thread.DestroyableThreadWrapper;
import com.rackspace.papi.service.event.PowerProxyEventKernel;
import com.rackspace.papi.service.event.PowerProxyEventManager;
import com.rackspace.papi.service.event.common.EventService;
import com.rackspace.papi.service.threading.ThreadingService;
import org.atomnuke.container.service.annotation.NukeService;
import org.atomnuke.container.service.annotation.Requires;
import org.atomnuke.lifecycle.InitializationException;
import org.atomnuke.service.ServiceContext;
import org.atomnuke.service.ServiceUnavailableException;
import org.atomnuke.service.runtime.AbstractRuntimeService;

/**
 *
 * @author zinic
 */
@NukeService
@Requires(ThreadingService.class)
public class EventServiceContext extends AbstractRuntimeService {

   private final PowerProxyEventKernel eventKernel;
   private final EventService eventService;

   private DestroyableThreadWrapper eventKernelThread;

   public EventServiceContext() {
      super(EventService.class);

      eventService = new PowerProxyEventManager();
      eventKernel = new PowerProxyEventKernel(eventService);
   }

   @Override
   public Object instance() {
      return eventService;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      try {
         final ThreadingService threadingService = context.services().firstAvailable(ThreadingService.class);

         eventKernelThread = new DestroyableThreadWrapper(threadingService.newThread(eventKernel, "Event Kernel Thread"), eventKernel);
         eventKernelThread.start();
      } catch (ServiceUnavailableException sue) {
      }
   }

   @Override
   public void destroy() {
      if (eventKernelThread != null) {
         eventKernelThread.destroy();
      }
   }
}
