package org.openrepose.components.apivalidator.filter;

import com.rackspace.com.papi.components.checker.Config;
import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.commons.config.parser.generic.GenericResourceConfigurationParser;
import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.commons.util.StringUtilities;
import com.rackspace.papi.filter.logic.AbstractConfiguredFilterHandlerFactory;
import com.rackspace.papi.service.config.ConfigurationService;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrepose.components.apivalidator.servlet.config.ValidatorConfiguration;
import org.openrepose.components.apivalidator.servlet.config.ValidatorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiValidatorHandlerFactory extends AbstractConfiguredFilterHandlerFactory<ApiValidatorHandler> {

    private static final Logger LOG = LoggerFactory.getLogger(ApiValidatorHandlerFactory.class);
    private ValidatorConfiguration validatorConfiguration;
    private ValidatorInfo defaultValidator;
    private List<ValidatorInfo> validators;
    private boolean initialized = false;
    private final ConfigurationService manager;
    private final ApiValidatorWadlListener wadlListener;
    private final Object lock;
    private final String configRoot;
    private boolean multiRoleMatch=false;

    public ApiValidatorHandlerFactory(ConfigurationService manager, String configurationRoot) {
        this.manager = manager;
        wadlListener = new ApiValidatorWadlListener();
        lock = new Object();
        this.configRoot = configurationRoot;
    }

    private void unsubscribeAll() {
        synchronized (lock) {
            initialized = false;
            if (validators == null) {
                return;
            }

            for (ValidatorInfo info : validators) {
                manager.unsubscribeFrom(info.getUri(), wadlListener);
            }
        }
    }

    ApiValidatorWadlListener getWadlListener() {
        return wadlListener;
    }

    void setValidators(List<ValidatorInfo> validators) {
        this.validators = validators;
    }

    class ApiValidatorWadlListener implements UpdateListener<ConfigurationResource> {

        private String getNormalizedPath(String uri) {
            String path = uri;
            try {
                path = new URL(uri).toString();
            } catch (MalformedURLException ex) {
                LOG.warn("Invalid URL: " + uri);
            }
            return path;
        }

        @Override
        public void configurationUpdated(ConfigurationResource config) {
            LOG.info("WADL file changed: " + config.name());

            synchronized (lock) {
                if (validators == null) {
                    return;
                }
                boolean found = false;

                for (ValidatorInfo info : validators) {
                    if (getNormalizedPath(info.getUri()).equals(config.name())) {
                        info.reinitValidator();
                        found = true;
                    }
                }

                if (!found) {
                    // If we couldn't match the particular config... be safe and clear 
                    // all fo the validators
                    for (ValidatorInfo info : validators) {
                       info.reinitValidator();
                    }
                }
            }
        }
    }

    private void addListener(String wadl) {
        LOG.info("Watching WADL: " + wadl);
        manager.subscribeTo(wadl, wadlListener, new GenericResourceConfigurationParser());
    }

    String getWadlPath(String wadl) {
       File file = new File(configRoot, wadl);
       
        return !wadl.contains("://") ? StringUtilities.join("file://", file.getAbsolutePath()) : wadl;
    }
    
    void initialize() {
        synchronized (lock) {
            if (initialized || validatorConfiguration == null) {
                return;
            }

            validators = new ArrayList<ValidatorInfo>(validatorConfiguration.getValidator().size());
            defaultValidator = null;
            multiRoleMatch=validatorConfiguration.isMultiRoleMatch();

            for (ValidatorItem validatorItem : validatorConfiguration.getValidator()) {
                Config config = new ValidatorConfigurator(validatorItem, multiRoleMatch, configRoot).getConfiguration();
                ValidatorInfo validator = new ValidatorInfo(validatorItem.getRole(), getWadlPath(validatorItem.getWadl()), config);

                validators.add (validator);
                if (validatorItem.isDefault() && defaultValidator == null) {
                    defaultValidator = validator;
                }
            }

            for (ValidatorInfo validator : validators) {
                addListener(validator.getUri());
            }

            initialized = true;
        }
    }

    private class ApiValidationConfigurationListener implements UpdateListener<ValidatorConfiguration> {

        @Override
        public void configurationUpdated(ValidatorConfiguration configurationObject) {
            validatorConfiguration = configurationObject;
            unsubscribeAll();
            initialize();
        }
    }

    void setValidatorCOnfiguration(ValidatorConfiguration configurationObject) {
        validatorConfiguration = configurationObject;
    }

    @Override
    protected ApiValidatorHandler buildHandler() {
        initialize();
        if (!initialized) {
            return null;
        }
        return new ApiValidatorHandler(defaultValidator, validators, multiRoleMatch);
    }

    @Override
    protected Map<Class, UpdateListener<?>> getListeners() {
        final Map<Class, UpdateListener<?>> updateListeners = new HashMap<Class, UpdateListener<?>>();
        updateListeners.put(ValidatorConfiguration.class, new ApiValidationConfigurationListener());
        return updateListeners;
    }
}
