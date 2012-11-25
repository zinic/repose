package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.logging.LoggingService;
import com.rackspace.papi.service.rms.ResponseMessageService;
import com.rackspace.papi.service.rms.ResponseMessageServiceImpl;
import com.rackspace.papi.service.rms.config.ResponseMessagingConfiguration;
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
@Requires(ConfigurationService.class)
public class ResponseMessageServiceContext extends AbstractRuntimeService {

   private class ResponseMessagingServiceListener implements UpdateListener<ResponseMessagingConfiguration> {

      private final ResponseMessageService messageService;

      public ResponseMessagingServiceListener(ResponseMessageService messageService) {
         this.messageService = messageService;
      }

      @Override
      public void configurationUpdated(ResponseMessagingConfiguration configurationObject) {
         messageService.updateConfiguration(configurationObject.getStatusCode());
      }
   }

   private final UpdateListener<ResponseMessagingConfiguration> configListener;
   private final ResponseMessageService responseMessageService;

   private ConfigurationService configurationService;

   public ResponseMessageServiceContext() {
      super(LoggingService.class);

      responseMessageService = new ResponseMessageServiceImpl();
      configListener = new ResponseMessagingServiceListener(responseMessageService);
   }

   @Override
   public Object instance() {
      return responseMessageService;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      try {
         configurationService = context.services().firstAvailable(ConfigurationService.class);
      configurationService.subscribeTo("response-messaging.cfg.xml", configListener, ResponseMessagingConfiguration.class);
      } catch (ServiceUnavailableException sue) {
         throw new InitializationException(sue);
      }
   }

   @Override
   public void destroy() {
      configurationService.unsubscribeFrom("response-messaging.cfg.xml", configListener);
   }
}
