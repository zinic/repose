package com.rackspace.papi.service.context.nuke.impl;

import com.rackspace.papi.service.datastore.DatastoreService;
import com.rackspace.papi.service.datastore.impl.PowerApiDatastoreService;
import com.rackspace.papi.service.datastore.impl.ehcache.EHCacheDatastoreManager;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.atomnuke.container.service.annotation.NukeService;
import org.atomnuke.lifecycle.InitializationException;
import org.atomnuke.service.ServiceContext;
import org.atomnuke.service.runtime.AbstractRuntimeService;

/**
 *
 * @author zinic
 */
@NukeService
public class DatastoreServiceContext extends AbstractRuntimeService {

   private static final String CACHE_MANAGER_NAME = "LocalDatastoreCacheManager";

   private final DatastoreService datastoreService;
   private CacheManager ehCacheManager;

   public DatastoreServiceContext() {
      super(DatastoreService.class);

      datastoreService = new PowerApiDatastoreService();
   }

   @Override
   public Object instance() {
      return datastoreService;
   }

   @Override
   public void init(ServiceContext context) throws InitializationException {
      // Init our local default cache and a new service object to hold it
      final Configuration defaultConfiguration = new Configuration();

      defaultConfiguration.setName(CACHE_MANAGER_NAME);
      defaultConfiguration.setDefaultCacheConfiguration(new CacheConfiguration().diskPersistent(false));
      defaultConfiguration.setUpdateCheck(false);

      // Init ehcache fu
      ehCacheManager = CacheManager.newInstance(defaultConfiguration);

      // regstier our local datastore
      datastoreService.registerDatastoreManager(DatastoreService.DEFAULT_LOCAL, new EHCacheDatastoreManager(ehCacheManager));
   }

   @Override
   public void destroy() {
      // No more ehcache, we're done
      ehCacheManager.removalAll();
      ehCacheManager.shutdown();
   }
}
