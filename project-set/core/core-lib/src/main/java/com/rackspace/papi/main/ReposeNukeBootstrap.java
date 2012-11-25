package com.rackspace.papi.main;

import org.atomnuke.StaticNukeEnvironment;
import org.atomnuke.container.boot.ContainerBootstrap;
import org.atomnuke.plugin.proxy.japi.JapiProxyFactory;
import org.atomnuke.service.RuntimeServiceManager;
import org.atomnuke.service.ServiceManager;

/**
 *
 * @author zinic
 */
public class ReposeNukeBootstrap {

   public static void main(String args[]) {
      final ServiceManager serviceManager = new RuntimeServiceManager(StaticNukeEnvironment.get(), new JapiProxyFactory());
      final ContainerBootstrap containerBootstrap = new ContainerBootstrap(StaticNukeEnvironment.get(), serviceManager);

      containerBootstrap.bootstrap();
   }
}
