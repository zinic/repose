package com.rackspace.papi.service.context.nuke.impl.cfg;

import com.rackspace.papi.service.config.impl.*;
import com.rackspace.papi.commons.config.manager.ConfigurationUpdateManager;
import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.commons.config.parser.common.ConfigurationParser;
import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.service.event.common.EventService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.atomnuke.plugin.context.LocalInstanceContext;
import org.atomnuke.task.TaskHandle;
import org.atomnuke.task.manager.service.TaskingService;
import org.atomnuke.util.TimeValue;
import org.atomnuke.util.lifecycle.runnable.ReclaimableTask;

public class RunnableConfigurationUpdateManager implements ConfigurationUpdateManager {

   private static final TimeValue DEFAULT_CFG_POLLING_INTERVAL = new TimeValue(15, TimeUnit.SECONDS);

   private final PowerApiUpdateManagerEventListener powerApiUpdateManagerEventListener;
   private final Map<String, Map<Integer, ParserListenerPair>> listenerMap;
   private final ConfigurationResourceWatcher resourceWatcher;
   private final TaskHandle resourceWatcherHandle;

   public RunnableConfigurationUpdateManager(TaskingService taskingService, EventService eventManager) {
      resourceWatcher = new ConfigurationResourceWatcher(eventManager);
      resourceWatcherHandle = taskingService.tasker().pollTask(new LocalInstanceContext<ReclaimableTask>(resourceWatcher), DEFAULT_CFG_POLLING_INTERVAL);

      listenerMap = new HashMap<String, Map<Integer, ParserListenerPair>>();
      powerApiUpdateManagerEventListener = new PowerApiUpdateManagerEventListener(listenerMap);

      eventManager.listen(powerApiUpdateManagerEventListener, ConfigurationEvent.class);
   }

   public PowerApiUpdateManagerEventListener getPowerApiUpdateManagerEventListener() {
      return powerApiUpdateManagerEventListener;
   }

   @Override
   public synchronized void destroy() {
      resourceWatcherHandle.cancellationRemote().cancel();
      listenerMap.clear();
   }

   @Override
   public synchronized <T> void registerListener(UpdateListener<T> listener, ConfigurationResource resource, ConfigurationParser<T> parser) {
      Map<Integer, ParserListenerPair> resourceListeners = listenerMap.get(resource.name());

      if (resourceListeners == null) {
         resourceListeners = new HashMap<Integer, ParserListenerPair>();

         listenerMap.put(resource.name(), resourceListeners);
         resourceWatcher.watch(resource);
      }

      resourceListeners.put(listener.hashCode(), new ParserListenerPair(listener, parser));
   }

   @Override
   public synchronized <T> void unregisterListener(UpdateListener<T> listener, ConfigurationResource resource) {
      Map<Integer, ParserListenerPair> resourceListeners = listenerMap.get(resource.name());

      if (resourceListeners != null) {
         resourceListeners.remove(listener.hashCode());

         if (resourceListeners.isEmpty()) {
            resourceWatcher.stopWatching(resource.name());
            listenerMap.remove(resource.name());
         }
      }
   }
}
