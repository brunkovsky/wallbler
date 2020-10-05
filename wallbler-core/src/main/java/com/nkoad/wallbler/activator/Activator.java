package com.nkoad.wallbler.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

public class Activator implements BundleActivator {

    public static ConfigurationAdmin configAdmin;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        configAdmin = getConfigAdmin(bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }

    private ConfigurationAdmin getConfigAdmin(BundleContext context) {
        ServiceReference ref = context.getServiceReference(ConfigurationAdmin.class.getName());
        return (ConfigurationAdmin) context.getService(ref);
    }

}
