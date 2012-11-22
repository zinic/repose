package com.rackspace.papi.service.context.container;

import com.rackspace.papi.domain.Port;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("containerConfigurationService")
public class ContainerConfigurationServiceImpl implements ContainerConfigurationService {

   private final List<Port> ports;

   private int contentBodyReadLimit;
   private String viaValue;

   public ContainerConfigurationServiceImpl() {
      ports = new ArrayList<Port>();
   }

   public ContainerConfigurationServiceImpl(List<Port> ports, String via, int contentBodyReadLimit) {
      this();

      this.ports.addAll(ports);
      this.viaValue = via;
      this.contentBodyReadLimit = contentBodyReadLimit;
   }

   @Override
   public List<Port> getPorts() {
      return ports;
   }

   @Override
   public String getVia() {
      return viaValue;
   }

   @Override
   public void setVia(String via) {
      this.viaValue = via;
   }

   @Override
   public int getContentBodyReadLimit() {
      if (contentBodyReadLimit <= 1) {
         return 0;
      } else {
         return contentBodyReadLimit;
      }
   }

   @Override
   public void setContentBodyReadLimit(int value) {
      this.contentBodyReadLimit = value;
   }
}
