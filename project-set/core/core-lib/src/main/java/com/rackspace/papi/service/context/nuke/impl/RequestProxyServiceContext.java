package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.container.config.ContainerConfiguration;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.proxy.RequestProxyService;
import com.rackspace.papi.service.proxy.jersey.RequestProxyServiceImpl;
import org.atomnuke.container.service.annotation.NukeService;
import org.atomnuke.container.service.annotation.Requires;
import org.atomnuke.lifecycle.InitializationException;
import org.atomnuke.service.ServiceUnavailableException;
import org.atomnuke.service.runtime.AbstractRuntimeService;

@NukeService
@Requires(ConfigurationService.class)
public class RequestProxyServiceContext extends AbstractRuntimeService {

   private class ContainerConfigListener implements UpdateListener<ContainerConfiguration> {

      private final RequestProxyService requestProxyService;

      public ContainerConfigListener(RequestProxyService requestProxyService) {
         this.requestProxyService = requestProxyService;
      }

      @Override
      public void configurationUpdated(ContainerConfiguration config) {
         final Integer connectionTimeout = config.getDeploymentConfig().getConnectionTimeout();
         final Integer readTimeout = config.getDeploymentConfig().getReadTimeout();
         final Integer proxyThreadPool = config.getDeploymentConfig().getProxyThreadPool();
         final boolean requestLogging = config.getDeploymentConfig().isClientRequestLogging();

         requestProxyService.updateConfiguration(connectionTimeout, readTimeout, proxyThreadPool, requestLogging);
      }
   }

   private final RequestProxyService proxyService;
   private final ContainerConfigListener configListener;
   private ConfigurationService configurationManager;

   public RequestProxyServiceContext() {
      super(RequestProxyService.class);

      proxyService = new RequestProxyServiceImpl();
      configListener = new ContainerConfigListener(proxyService);
   }

   @Override
   public Object instance() {
      return proxyService;
   }

   @Override
   public void init(org.atomnuke.service.ServiceContext context) throws InitializationException {
      try {
         configurationManager = context.services().firstAvailable(ConfigurationService.class);
         configurationManager.subscribeTo("container.cfg.xml", configListener, ContainerConfiguration.class);
      } catch(ServiceUnavailableException sue) {
         throw new InitializationException(sue);
      }
   }

   @Override
   public void destroy() {
         configurationManager.unsubscribeFrom("container.cfg.xml", configListener);
   }
}
