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
import java.util.Date;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import chronos.mbeans.QuartzSchedulerAdapter;

/**
 * @author Alistair A. Israel
 */
public final class ChronosServletListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(ChronosServletListener.class);

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(final ServletContextEvent event) {
        logger.debug("Chronos is starting up...");

        final MBeanServer mbeanServer = findOrCreateChronosMBeanServer();

        initializeQuartzScheduler(mbeanServer);
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
    private void initializeQuartzScheduler(final MBeanServer mbeanServer) {
        try {
            final ObjectName objectName = new ObjectName(CHRONOS, "type", "QuartzSchedulerAdapter");
            logger.debug("Creating and registering QuartzSchedulerAdapter MBean...");
            final QuartzSchedulerAdapter quartzSchedulerAdapter = new QuartzSchedulerAdapter();
            mbeanServer.registerMBean(quartzSchedulerAdapter, objectName);

            logger.debug("Invoking start() on QuartzSchedulerAdapter MBean...");
            mbeanServer.invoke(objectName, "start", null, null);

        } catch (final MalformedObjectNameException e) {
            logger.error("Registering QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final NullPointerException e) {
            logger.error("Registering QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final InstanceAlreadyExistsException e) {
            logger.error("Registering QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final MBeanRegistrationException e) {
            logger.error("Registering QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final NotCompliantMBeanException e) {
            logger.error("Registering QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final InstanceNotFoundException e) {
            logger.error("Invoking start() on QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final ReflectionException e) {
            logger.error("Invoking start() on QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final MBeanException e) {
            logger.error("Invoking start() on QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        }
    }

    /**
     * @param scheduler
     *        the {@link Scheduler}
     * @throws SchedulerException
     *         on exception
     */
    private void initializeQuartzJob(final Scheduler scheduler) throws SchedulerException {
        try {
            final JobDetail jobDetail = new JobDetail("TestJob", CHRONOS, TestJob.class);
            final JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(InitialContext.class.getName(), new InitialContext());

            final Trigger trigger = TriggerUtils.makeMinutelyTrigger();
            trigger.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));
            trigger.setName("TestTrigger");

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (final NamingException e) {
            logger.error("JNDI Exception: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(final ServletContextEvent event) {
        logger.debug("Chronos is shutting down...");
        final MBeanServer mbeanServer = findOrCreateChronosMBeanServer();
        try {
            final ObjectName objectName = new ObjectName(CHRONOS, "type", "QuartzSchedulerAdapter");

            logger.debug("Invoking shutdown() on QuartzSchedulerAdapter...");
            mbeanServer.invoke(objectName, "shutdown", null, null);

            logger.debug("Unregistering QuartzSchedulerAdapter Mbean");
            mbeanServer.unregisterMBean(objectName);
        } catch (final MalformedObjectNameException e) {
            logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final NullPointerException e) {
            logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final InstanceNotFoundException e) {
            logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final ReflectionException e) {
            logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        } catch (final MBeanException e) {
            logger.error("Shutting down QuartzSchedulerAdapter failed: " + e.getMessage(), e);
        }
        logger.info("Chronos shutdown");
    }

}
