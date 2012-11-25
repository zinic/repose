package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.container.config.ContainerConfiguration;
import com.rackspace.papi.container.config.DeploymentConfiguration;
import com.rackspace.papi.domain.Port;
import com.rackspace.papi.domain.ServicePorts;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.context.container.ContainerConfigurationService;
import com.rackspace.papi.service.context.container.ContainerConfigurationServiceImpl;
import com.rackspace.papi.servlet.InitParameter;
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
@Requires({ConfigurationService.class})
public class ContainerConfigurationContext extends AbstractRuntimeService {

   private static final Logger LOG = LoggerFactory.getLogger(ContainerConfigurationContext.class);

   private final ContainerConfigurationService containerConfigurationService;
   private final UpdateListener<ContainerConfiguration> containerCfgUpdateListener;
   private ConfigurationService configurationService;

   public ContainerConfigurationContext() {
      super(ContainerConfigurationService.class);

      containerCfgUpdateListener = new ContainerConfigurationListener();
      containerConfigurationService = new ContainerConfigurationServiceImpl();
   }

   @Override
   public Object instance() {
      return containerConfigurationService;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      try {
         configurationService = context.services().firstAvailable(ConfigurationService.class);
         configurationService.subscribeTo("container.cfg.xml", containerCfgUpdateListener, ContainerConfiguration.class);
      } catch (ServiceUnavailableException sue) {
         throw new InitializationException(sue);
      }
   }

   @Override
   public void destroy() {
      configurationService.unsubscribeFrom("container.cfg.xml", containerCfgUpdateListener);
   }

   /**
    * Listens for updates to the container.cfg.xml file which holds the location
    * of the log properties file.
    */
   private class ContainerConfigurationListener implements UpdateListener<ContainerConfiguration> {

      private ServicePorts determinePorts(DeploymentConfiguration deployConfig) {
         ServicePorts ports = new ServicePorts();

         if (deployConfig != null) {
            if (deployConfig.getHttpPort() != null) {
               ports.add(new Port("http", deployConfig.getHttpPort()));
            } else {
               LOG.error("Http service port not specified in container.cfg.xml");
            }

            if (deployConfig.getHttpsPort() != null) {
               ports.add(new Port("https", deployConfig.getHttpsPort()));
            } else {
               LOG.info("Https service port not specified in container.cfg.xml");
            }
         }

         return ports;
      }

      @Override
      public void configurationUpdated(ContainerConfiguration configurationObject) {
         final DeploymentConfiguration deployConfig = configurationObject.getDeploymentConfig();
         final ServicePorts configuredPorts = determinePorts(deployConfig);
         final String via = deployConfig.getVia();

         int maxResponseContentSize = deployConfig.getContentBodyReadLimit().intValue();

         if (containerConfigurationService.getPorts().isEmpty()) {
            // no ports have been set yet

            if (!configuredPorts.isEmpty()) {
               LOG.info("Setting " + InitParameter.PORT.getParameterName() + " to " + configuredPorts);

               containerConfigurationService.setVia(via);
               containerConfigurationService.setArtifactDirectory(deployConfig.getArtifactDirectory().getValue());
               containerConfigurationService.setDeploymentDirectory(deployConfig.getDeploymentDirectory().getValue());
               containerConfigurationService.setContentBodyReadLimit(maxResponseContentSize);
               containerConfigurationService.getPorts().addAll(configuredPorts);
            } else {
               // current port and port specified in container.cfg.xml are -1 (not set)
               LOG.error("Cannot determine " + InitParameter.PORT.getParameterName() + ". Port must be specified in container.cfg.xml or on the command line.");
            }
         } else {
            if (!containerConfigurationService.getPorts().equals(configuredPorts)) {
               // Port changed and is different from port already available
               LOG.warn("****** " + InitParameter.PORT.getParameterName() + " changed from " + containerConfigurationService.getPorts() + " --> " + configuredPorts
                       + ".  Restart is required for this change.");
            } else {
               containerConfigurationService.setVia(via);
               containerConfigurationService.setContentBodyReadLimit(maxResponseContentSize);
            }
         }

      }
   }
}
