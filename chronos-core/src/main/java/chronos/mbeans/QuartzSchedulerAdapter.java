/**
 *
 */
package chronos.mbeans;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Alistair A. Israel
 */
public class QuartzSchedulerAdapter implements QuartzSchedulerAdapterMBean {

    private static final Log logger = LogFactory.getLog(QuartzSchedulerAdapter.class);

    private final AtomicReference<Scheduler> schedulerRef = new AtomicReference<Scheduler>();

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#getQuartzVersion()
     */
    @Override
    public String getQuartzVersion() {
        return QuartzScheduler.getVersionMajor() + "." + QuartzScheduler.getVersionMinor() + "."
                + QuartzScheduler.getVersionIteration();
    }

    public boolean isQuartzRunning() {
        return schedulerRef.get() != null;
    }

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#start()
     */
    @Override
    public void start() {
        if (schedulerRef.get() == null) {
            logger.debug("Creating new Quartz scheduler...");
            try {
                final StdSchedulerFactory factory = new StdSchedulerFactory();
                final ClassPathResource resource = new ClassPathResource("quartz.properties");
                if (resource.exists() && resource.isReadable()) {
                    logger.debug("Configuring Quartz from resource: " + resource.getPath());
                    try {
                        final InputStream is = resource.getInputStream();
                        try {
                            factory.initialize(is);
                        } finally {
                            is.close();
                        }
                    } catch (final IOException e) {
                        logger.debug("Exception initializing from resource: " + e.getMessage(), e);
                    }
                } else {
                    logger.debug("Using default Quartz configuration");
                    factory.initialize();
                }
                final Scheduler scheduler = factory.getScheduler();
                if (schedulerRef.compareAndSet(null, scheduler)) {
                    logger.debug("Quartz scheduler successfully created. Starting...");
                    try {
                        scheduler.start();
                        logger.debug("Quartz started up successfully");
                    } catch (final SchedulerException e) {
                        logger.error("Scheduler start() failed!: " + e.getMessage(), e);
                    }
                } else {
                    logger.warn("Tried to set schedulerRef, expecting null but was already set!");
                }
            } catch (final SchedulerException e) {
                logger.error("Initializing scheduler failed!: " + e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#shutdown()
     */
    @Override
    public void shutdown() {
        final Scheduler scheduler = schedulerRef.get();
        if (scheduler != null) {
            logger.debug("Quartz scheduler shutting down...");
            try {
                scheduler.shutdown();
                final boolean nulled = schedulerRef.compareAndSet(scheduler, null);
                if (!nulled) {
                    logger.warn("Tried to set schedulerRef to null but was set to a different value!");
                }
            } catch (final SchedulerException e) {
                logger.error("Encountered exception shutting down Quartz: " + e.getMessage(), e);
            }
        } else {
            logger.debug("scheduler is null!");
        }
    }
}
