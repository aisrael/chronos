/**
 * Copyright (c) 2009-2010 Alistair A. Israel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created Feb 1, 2010
 */
package chronos.web.listener;

import static chronos.Chronos.CHRONOS;

import java.util.ArrayList;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chronos.mbeans.QuartzSchedulerAdapter;

/**
 * @author Alistair A. Israel
 */
public final class ChronosServletListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(ChronosServletListener.class);

    /**
     *
     */
    private static final String QUARTZ_SCHEDULER_ADAPTER = "QuartzSchedulerAdapter";

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(final ServletContextEvent event) {
        logger.debug("Chronos is starting up...");

        final MBeanServer mbeanServer = findOrCreateChronosMBeanServer();

        initializeQuartzSchedulerAdapter(mbeanServer);
        logger.info("Chronos started up");
    }

    /**
     * @return MBeanServer
     */
    private MBeanServer findOrCreateChronosMBeanServer() {
        final ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        logger.debug("Got " + servers.size() + " servers");
        if (servers.size() == 0) {
            logger.debug("Creating new MBeanServer for \"chronos\"...");
            return MBeanServerFactory.createMBeanServer(CHRONOS);
        }

        final MBeanServer server = servers.get(0);
        logger.debug("Returning MBeanServer domain: " + server.getDefaultDomain());
        return server;
    }

    /**
     * @param mbeanServer
     *        the MBeanServer
     */
    private void initializeQuartzSchedulerAdapter(final MBeanServer mbeanServer) {
        try {
            logger.debug("Creating and registering QuartzSchedulerAdapter MBean...");

            final ObjectName objectName = new ObjectName(CHRONOS, "type", QUARTZ_SCHEDULER_ADAPTER);

            final QuartzSchedulerAdapter quartzSchedulerAdapter = new QuartzSchedulerAdapter();
            mbeanServer.registerMBean(quartzSchedulerAdapter, objectName);

            logger.debug("Invoking start() on QuartzSchedulerAdapter MBean...");
            try {
                mbeanServer.invoke(objectName, "start", null, null);
            } catch (final JMException e) {
                logger.error("QuartzSchedulerAdapter start() failed: " + e.getMessage(), e);
            }
        } catch (final JMException e) {
            logger.error("Registering QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(final ServletContextEvent event) {
        logger.debug("Chronos is shutting down...");
        final ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        logger.debug("Got " + servers.size() + " servers");
        if (servers.size() != 0) {
            final MBeanServer mbeanServer = servers.get(0);
            try {
                final ObjectName objectName = new ObjectName(CHRONOS, "type", QUARTZ_SCHEDULER_ADAPTER);

                logger.debug("Invoking shutdown() on QuartzSchedulerAdapter...");
                mbeanServer.invoke(objectName, "shutdown", null, null);

                logger.debug("Unregistering QuartzSchedulerAdapter Mbean");
                mbeanServer.unregisterMBean(objectName);
            } catch (final JMException e) {
                logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
            } catch (final NullPointerException e) {
                logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
            }
        } else {
            logger.warn("Unable to find MBeanServer!");
        }
        logger.info("Chronos shutdown");
    }

}
