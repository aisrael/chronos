/**
 *
 */
package chronos.web.listener;

import java.util.Date;

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
public class ChronosServletListener implements ServletContextListener {

    private static final Log logger = LogFactory
            .getLog(ChronosServletListener.class);

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(final ServletContextEvent event) {
        logger.debug("Chronos is starting up...");
        logger.debug("Creating and starting up Quartz scheduler...");
        try {
            final DirectSchedulerFactory factory = DirectSchedulerFactory
                    .getInstance();
            if (factory != null) {
                factory.createVolatileScheduler(8);
                final Scheduler scheduler = factory.getScheduler();
                if (scheduler != null) {
                    scheduler.start();
                    logger.debug("Quartz started up successfully");
                    initializeQuartzJob(scheduler);
                } else {
                    logger.warn("DirectSchedulerFactory.getScheduler()"
                            + " returned null!");
                }
            } else {
                logger.warn("DirectSchedulerFactory.getInstance()"
                        + " returned null!");
            }
        } catch (final SchedulerException e) {
            logger.error("Initializing Quartz scheduler failed: "
                    + e.getMessage(), e);
        }
        logger.info("Chronos started up");
    }

    /**
     * @param scheduler
     * @throws SchedulerException
     */
    private void initializeQuartzJob(final Scheduler scheduler)
            throws SchedulerException {
        try {
            final JobDetail jobDetail = new JobDetail("TestJob", TestJob.class);
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
            final DirectSchedulerFactory factory = DirectSchedulerFactory
                    .getInstance();
            if (factory != null) {
                final Scheduler scheduler = factory.getScheduler();
                if (scheduler != null) {
                    scheduler.shutdown();
                    logger.info("Quartz shutdown successfully");
                } else {
                    logger.warn("DirectSchedulerFactory.getScheduler()"
                            + " returned null!");
                }
            } else {
                logger.warn("DirectSchedulerFactory.getInstance()"
                        + " returned null!");
            }
        } catch (final SchedulerException e) {
            logger.error("Encountered exception shutting down Quartz: "
                    + e.getMessage(), e);
        }
        logger.info("Chronos has shutdown");
    }

}
