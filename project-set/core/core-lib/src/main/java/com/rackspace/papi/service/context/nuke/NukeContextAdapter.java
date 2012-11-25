package com.rackspace.papi.service.context.nuke;

import com.rackspace.papi.filter.PowerFilterChainBuilder;
import com.rackspace.papi.service.classloader.ClassLoaderManagerService;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.context.ContextAdapter;
import com.rackspace.papi.service.context.ServiceContext;
import com.rackspace.papi.service.context.container.ContainerConfigurationService;
import com.rackspace.papi.service.datastore.DatastoreService;
import com.rackspace.papi.service.event.common.EventService;
import com.rackspace.papi.service.filterchain.GarbageCollectionService;
import com.rackspace.papi.service.headers.request.RequestHeaderService;
import com.rackspace.papi.service.headers.response.ResponseHeaderService;
import com.rackspace.papi.service.logging.LoggingService;
import com.rackspace.papi.service.proxy.RequestProxyService;
import com.rackspace.papi.service.reporting.ReportingService;
import com.rackspace.papi.service.rms.ResponseMessageService;
import com.rackspace.papi.service.routing.RoutingService;
import com.rackspace.papi.service.threading.ThreadingService;
import org.atomnuke.service.ServiceManager;
import org.atomnuke.service.ServiceUnavailableException;
import org.atomnuke.service.introspection.ServicesInterrogator;
import org.atomnuke.service.introspection.ServicesInterrogatorImpl;

/**
 *
 * @author zinic
 */
public class NukeContextAdapter implements ContextAdapter {

   private final ServicesInterrogator servicesInterrogator;

   public NukeContextAdapter(ServiceManager serviceManager) {
      this.servicesInterrogator = new ServicesInterrogatorImpl(serviceManager);
   }

   @Override
   public ClassLoaderManagerService classLoader() {
      try {
         return servicesInterrogator.firstAvailable(ClassLoaderManagerService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public EventService eventService() {
      try {
         return servicesInterrogator.firstAvailable(EventService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public ThreadingService threadingService() {
      try {
         return servicesInterrogator.firstAvailable(ThreadingService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public DatastoreService datastoreService() {
      try {
         return servicesInterrogator.firstAvailable(DatastoreService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public ConfigurationService configurationService() {
      try {
         return servicesInterrogator.firstAvailable(ConfigurationService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public ContainerConfigurationService containerConfigurationService() {
      try {
         return servicesInterrogator.firstAvailable(ContainerConfigurationService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public GarbageCollectionService filterChainGarbageCollectorService() {
      try {
         return servicesInterrogator.firstAvailable(GarbageCollectionService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public ResponseMessageService responseMessageService() {
      try {
         return servicesInterrogator.firstAvailable(ResponseMessageService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public LoggingService loggingService() {
      try {
         return servicesInterrogator.firstAvailable(LoggingService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public RoutingService routingService() {
      try {
         return servicesInterrogator.firstAvailable(RoutingService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public RequestProxyService requestProxyService() {
      try {
         return servicesInterrogator.firstAvailable(RequestProxyService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public ReportingService reportingService() {
      try {
         return servicesInterrogator.firstAvailable(ReportingService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public String getReposeVersion() {
      return "bugjohn";
   }

   @Override
   public RequestHeaderService requestHeaderService() {
      try {
         return servicesInterrogator.firstAvailable(RequestHeaderService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public ResponseHeaderService responseHeaderService() {
      try {
         return servicesInterrogator.firstAvailable(ResponseHeaderService.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public PowerFilterChainBuilder filterChainBuilder() {
      try {
         return servicesInterrogator.firstAvailable(PowerFilterChainBuilder.class);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }

   @Override
   public <T extends ServiceContext<?>> T getContext(Class<T> clazz) {
      try {
         return servicesInterrogator.firstAvailable(clazz);
      } catch (ServiceUnavailableException exception) {
         return null;
      }
   }
}
