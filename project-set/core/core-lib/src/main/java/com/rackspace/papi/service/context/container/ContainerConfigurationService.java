package com.rackspace.papi.service.context.container;

import com.rackspace.papi.domain.Port;
import java.util.List;

// Container configuration service... this may be better served by utilizing a
// richer context object during init of services and actors that might be
// interested in these values
public interface ContainerConfigurationService {

   String getDeploymentDirectory();

   void setDeploymentDirectory(String value);

   String getArtifactDirectory();

   void setArtifactDirectory(String value);

   List<Port> getPorts();

   String getVia();

   void setVia(String via);

   int getContentBodyReadLimit();

   void setContentBodyReadLimit(int value);
}
