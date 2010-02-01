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

import java.util.ArrayList;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
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
import org.quartz.impl.DirectSchedulerFactory;

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
        for (final MBeanServer server : servers) {
            logger.debug(server.getDefaultDomain());
            if ("Chronos".equals(server.getDefaultDomain())) {
                logger.debug("Found existing MBeanServer for \"Chronos\"...");
                return server;
            }
        }

        logger.debug("Creating new MBeanServer for \"Chronos\"...");
        return MBeanServerFactory.createMBeanServer("Chronos");
    }

    /**
     * @param mbeanServer
     *        the MBeanServer
     */
    private void initializeQuartzScheduler(final MBeanServer mbeanServer) {
        logger.debug("Creating and starting up Quartz scheduler...");
        try {
            final DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
            if (factory != null) {
                factory.createVolatileScheduler(8);
                final Scheduler scheduler = factory.getScheduler();
                if (scheduler != null) {
                    scheduler.start();
                    logger.debug("Quartz started up successfully");
                    final String[] jobNames = scheduler.getJobNames("Chronos");
                    logger.debug("Got " + jobNames.length + " job names under group \"Chronos\"");
                    for (final String jobName : jobNames) {
                        logger.debug(jobName);
                    }
                    initializeQuartzJob(scheduler);
                } else {
                    logger.warn("DirectSchedulerFactory.getScheduler()" + " returned null!");
                }
            } else {
                logger.warn("DirectSchedulerFactory.getInstance()" + " returned null!");
            }
        } catch (final SchedulerException e) {
            logger.error("Initializing Quartz scheduler failed: " + e.getMessage(), e);
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
            final JobDetail jobDetail = new JobDetail("TestJob", "Chronos", TestJob.class);
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
        logger.debug("Shutting down Quartz...");
        try {
            final DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
            if (factory != null) {
                final Scheduler scheduler = factory.getScheduler();
                if (scheduler != null) {
                    scheduler.shutdown();
                    logger.info("Quartz shutdown successfully");
                } else {
                    logger.warn("DirectSchedulerFactory.getScheduler()" + " returned null!");
                }
            } else {
                logger.warn("DirectSchedulerFactory.getInstance()" + " returned null!");
            }
        } catch (final SchedulerException e) {
            logger.error("Encountered exception shutting down Quartz: " + e.getMessage(), e);
        }
        logger.info("Chronos has shutdown");
    }

}
