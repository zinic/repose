/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.papi.service.context.nuke;

import com.rackspace.papi.service.context.ContextAdapter;
import com.rackspace.papi.service.context.ContextAdapterProvider;
import javax.naming.Context;

/**
 *
 * @author zinic
 */
public class NukeContextAdapterProvider implements ContextAdapterProvider {

   @Override
   public ContextAdapter newInstance(Context context) {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
