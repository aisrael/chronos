/**
 *
 */
package chronos.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
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
            DirectSchedulerFactory.getInstance().createVolatileScheduler(8);
            logger.debug("Quartz started up successfully");
        } catch (final SchedulerException e) {
            logger.error("Initializing Quartz scheduler failed: "
                    + e.getMessage(), e);
        }
        logger.info("Chronos started up");
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
            DirectSchedulerFactory.getInstance().getScheduler().shutdown();
            logger.info("Quartz shutdown successfully");
        } catch (final SchedulerException e) {
            logger.error("Encountered exception shutting down Quartz: "
                    + e.getMessage(), e);
        }
        logger.info("Chronos has shutdown");
    }

}
