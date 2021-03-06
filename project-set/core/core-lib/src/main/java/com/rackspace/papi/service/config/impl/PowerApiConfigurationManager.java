package com.rackspace.papi.service.config.impl;

import com.rackspace.papi.commons.config.ConfigurationResourceException;
import com.rackspace.papi.commons.config.manager.ConfigurationUpdateManager;
import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.commons.config.parser.ConfigurationParserFactory;
import com.rackspace.papi.commons.config.parser.common.ConfigurationParser;
import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.commons.config.resource.ConfigurationResourceResolver;
import com.rackspace.papi.service.config.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Component("configurationManager")
public class PowerApiConfigurationManager implements ConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(PowerApiConfigurationManager.class);
    private final Map<Class, WeakReference<ConfigurationParser>> parserLookaside;
    private ConfigurationUpdateManager updateManager;
    private ConfigurationResourceResolver resourceResolver;

    @Autowired
    public PowerApiConfigurationManager(@Qualifier("reposeVersion") String version) {
        LOG.error("Repose Version: " + version);
        parserLookaside = new HashMap<Class, WeakReference<ConfigurationParser>>();
    }

    @Override
    public void destroy() {
        parserLookaside.clear();
        updateManager.destroy();
    }

    @Override
    public void setResourceResolver(ConfigurationResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @Override
    public void setUpdateManager(ConfigurationUpdateManager updateManager) {
        this.updateManager = updateManager;
    }

    @Override
    public <T> void subscribeTo(String configurationName, UpdateListener<T> listener, Class<T> configurationClass) {
        subscribeTo(configurationName, listener, getPooledJaxbConfigurationParser(configurationClass));
    }

    @Override
    public <T> void subscribeTo(String configurationName, UpdateListener<T> listener, ConfigurationParser<T> customParser) {
        subscribeTo(configurationName, listener, customParser, true);
    }

    @Override
    public <T> void subscribeTo(String configurationName, UpdateListener<T> listener, ConfigurationParser<T> customParser, boolean sendNotificationNow) {
        final ConfigurationResource resource = resourceResolver.resolve(configurationName);
        updateManager.registerListener(listener, resource, customParser);

        if (sendNotificationNow) {
            // Initial load of the cfg object
            try {
                listener.configurationUpdated(customParser.read(resource));
            } catch (Exception ex) {

                // TODO:Refactor - Introduce a helper method so that this logic can be centralized and reused
                if (ex.getCause() instanceof FileNotFoundException) {
                    LOG.error("An I/O error has occured while processing resource " + configurationName + " - Reason: " + ex.getCause().getMessage());
                } else {
                    LOG.error("Configuration update error. Reason: " + ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public void unsubscribeFrom(String configurationName, UpdateListener listener) {
        updateManager.unregisterListener(listener, resourceResolver.resolve(configurationName));
    }

    public <T> ConfigurationParser<T> getPooledJaxbConfigurationParser(Class<T> configurationClass) {
        final WeakReference<ConfigurationParser> parserReference = parserLookaside.get(configurationClass);
        ConfigurationParser<T> parser = parserReference != null ? parserReference.get() : null;

        if (parser == null) {
            try {
                parser = ConfigurationParserFactory.getXmlConfigurationParser(configurationClass);
            } catch (ConfigurationResourceException cre) {
                throw new ConfigurationServiceException("Failed to create a JAXB context for a configuration parser. Reason: " + cre.getMessage(), cre);
            }

            parserLookaside.put(configurationClass, new WeakReference<ConfigurationParser>(parser));
        }

        return parser;
    }
}
