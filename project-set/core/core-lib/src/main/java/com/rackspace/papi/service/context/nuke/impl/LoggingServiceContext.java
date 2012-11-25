package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.commons.config.parser.properties.PropertiesFileConfigurationParser;
import com.rackspace.papi.commons.util.StringUtilities;
import com.rackspace.papi.container.config.ContainerConfiguration;
import com.rackspace.papi.container.config.LoggingConfiguration;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.logging.LoggingService;
import com.rackspace.papi.service.logging.LoggingServiceImpl;
import com.rackspace.papi.service.logging.common.LogFrameworks;
import java.util.Properties;
import org.atomnuke.container.service.annotation.NukeService;
import org.atomnuke.container.service.annotation.Requires;
import org.atomnuke.lifecycle.InitializationException;
import org.atomnuke.service.ServiceContext;
import org.atomnuke.service.ServiceUnavailableException;
import org.atomnuke.service.runtime.AbstractRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
@NukeService
@Requires(ConfigurationService.class)
public class LoggingServiceContext extends AbstractRuntimeService {

   private final Logger LOG = LoggerFactory.getLogger(LoggingServiceContext.class);

   /**
    * Listens for updates to the container.cfg.xml file which holds the location
    * of the log properties file.
    */
   private class ContainerConfigurationListener implements UpdateListener<ContainerConfiguration> {

      @Override
      public void configurationUpdated(ContainerConfiguration configurationObject) {

         if (configurationObject.getDeploymentConfig() != null) {
            final LoggingConfiguration loggingConfig = configurationObject.getDeploymentConfig().getLoggingConfiguration();

            if (loggingConfig != null && !StringUtilities.isBlank(loggingConfig.getHref())) {
               final String newLoggingConfig = loggingConfig.getHref();
               loggingConfigurationLocation = newLoggingConfig;
               updateLogConfigFileSubscription(loggingConfigurationLocation, newLoggingConfig);
            }
         }
      }
   }

   /**
    * Listens for updates to the log properties file.
    */
   private class LoggingConfigurationListener implements UpdateListener<Properties> {

      @Override
      public void configurationUpdated(Properties configurationObject) {
         loggingService.updateLoggingConfiguration(configurationObject);

         LOG.error("ERROR LEVEL LOG STATEMENT");
         LOG.warn("WARN LEVEL LOG STATEMENT");
         LOG.info("INFO LEVEL LOG STATEMENT");
         LOG.debug("DEBUG LEVEL LOG STATEMENT");
         LOG.trace("TRACE LEVEL LOG STATEMENT");
      }
   }

   private final ContainerConfigurationListener configurationListener;
   private final LoggingConfigurationListener loggingConfigurationListener;
   private final LoggingService loggingService;

   private ConfigurationService configurationService;
   private String loggingConfigurationLocation;

   public LoggingServiceContext() {
      super(LoggingService.class);

      // Because I'm lazy like that
      loggingService = new LoggingServiceImpl(LogFrameworks.LOG4J);

      loggingConfigurationLocation = "";

      this.configurationListener = new ContainerConfigurationListener();
      this.loggingConfigurationListener = new LoggingConfigurationListener();
   }

   @Override
   public Object instance() {
      return loggingService;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      try {
         configurationService = context.services().firstAvailable(ConfigurationService.class);
         configurationService.subscribeTo("container.cfg.xml", configurationListener, ContainerConfiguration.class);
      } catch (ServiceUnavailableException sue) {
         throw new InitializationException(sue);
      }
   }

   @Override
   public void destroy() {
      configurationService.unsubscribeFrom("container.cfg.xml", configurationListener);
      configurationService.unsubscribeFrom(loggingConfigurationLocation, loggingConfigurationListener);
   }

   private void updateLogConfigFileSubscription(String oldLocation, String newLocation) {
      configurationService.unsubscribeFrom(oldLocation, loggingConfigurationListener);
      configurationService.subscribeTo(newLocation, loggingConfigurationListener, new PropertiesFileConfigurationParser());
   }
}
