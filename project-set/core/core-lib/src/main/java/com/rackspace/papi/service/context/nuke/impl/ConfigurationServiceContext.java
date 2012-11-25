package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.config.resource.impl.FileDirectoryResourceResolver;
import com.rackspace.papi.commons.util.StringUtilities;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.config.impl.PowerApiConfigurationManager;
import com.rackspace.papi.service.context.nuke.impl.cfg.RunnableConfigurationUpdateManager;
import com.rackspace.papi.service.event.common.EventService;
import com.rackspace.papi.servlet.InitParameter;
import com.rackspace.papi.servlet.PowerApiContextException;
import org.atomnuke.container.service.annotation.NukeService;
import org.atomnuke.container.service.annotation.Requires;
import org.atomnuke.lifecycle.InitializationException;
import org.atomnuke.service.ServiceContext;
import org.atomnuke.service.ServiceUnavailableException;
import org.atomnuke.service.runtime.AbstractRuntimeService;
import org.atomnuke.task.manager.service.TaskingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
@NukeService
@Requires({TaskingService.class, EventService.class})
public class ConfigurationServiceContext extends AbstractRuntimeService {

   private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceContext.class);

   private ConfigurationService service;

   public ConfigurationServiceContext() {
      super(ConfigurationService.class);
   }

   @Override
   public Object instance() {
      return service;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      final String configurationRoot = context.environment().fromEnv("REPOSE_CFG_DIR", "/etc/repose/");

      LOG.debug("Loading configuration files from directory: " + configurationRoot);

      if (StringUtilities.isBlank(configurationRoot)) {
         throw new PowerApiContextException(
                 "Power API requires a configuration directory to be specified as an init-param named, \""
                 + InitParameter.POWER_API_CONFIG_DIR.getParameterName() + "\"");
      }

      try {
         final EventService eventService = context.services().firstAvailable(EventService.class);
         final TaskingService taskingService = context.services().firstAvailable(TaskingService.class);

         final RunnableConfigurationUpdateManager papiUpdateManager = new RunnableConfigurationUpdateManager(taskingService, eventService);

         service = new PowerApiConfigurationManager(configurationRoot);
         service.setResourceResolver(new FileDirectoryResourceResolver(configurationRoot));
         service.setUpdateManager(papiUpdateManager);
      } catch (ServiceUnavailableException sue) {
         throw new InitializationException(sue);
      }
   }

   @Override
   public void destroy() {
      super.destroy();
   }
}
