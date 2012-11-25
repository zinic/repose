package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.model.SystemModel;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.routing.RoutingService;
import com.rackspace.papi.service.routing.robin.RoundRobinRoutingService;
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
@Requires({ConfigurationService.class})
public class RoutingServiceContext extends AbstractRuntimeService {

   private class PowerApiConfigListener implements UpdateListener<SystemModel> {

      private final RoutingService service;

      public PowerApiConfigListener(RoutingService service) {
         this.service = service;
      }

      @Override
      public void configurationUpdated(SystemModel configurationObject) {
         service.setSystemModel(configurationObject);
      }
   }

   private final RoutingService routingService;
   private final UpdateListener<SystemModel> configListener;

   private ConfigurationService configurationManager;

   public RoutingServiceContext() {
      super(RoutingService.class);

      routingService = new RoundRobinRoutingService();
      configListener = new PowerApiConfigListener(routingService);
   }

   @Override
   public Object instance() {
      return routingService;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      try {
         configurationManager = context.services().firstAvailable(ConfigurationService.class);
         configurationManager.subscribeTo("system-model.cfg.xml", configListener, SystemModel.class);
      } catch (ServiceUnavailableException sue) {
         throw new InitializationException(sue);
      }
   }

   @Override
   public void destroy() {
      configurationManager.unsubscribeFrom("system-model.cfg.xml", configListener);
   }
}
