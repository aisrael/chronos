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
 * Created Mar 12, 2010
 */
package chronos.mbeans;

import static org.quartz.core.QuartzSchedulerResources.getUniqueIdentifier;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME;
import static org.quartz.impl.StdSchedulerFactory.PROP_THREAD_POOL_CLASS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import chronos.Chronos;
import chronos.TestJob;

/**
 * @author Alistair A. Israel
 */
public class QuartzSchedulerAdapter implements QuartzSchedulerAdapterMBean {

    private static final Log logger = LogFactory.getLog(QuartzSchedulerAdapter.class);

    private final String schedulerInstanceName = Chronos.CHRONOS + "." + System.identityHashCode(this);

    private final AtomicReference<Scheduler> schedulerRef = new AtomicReference<Scheduler>();

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#getQuartzVersion()
     */
    @Override
    public final String getQuartzVersion() {
        return QuartzScheduler.getVersionMajor() + "." + QuartzScheduler.getVersionMinor() + "."
                + QuartzScheduler.getVersionIteration();
    }

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#isQuartzRunning()
     */
    public final boolean isQuartzRunning() {
        return schedulerRef.get() != null;
    }

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#start()
     */
    @Override
    public final void start() {
        if (schedulerRef.get() == null) {
            final StdSchedulerFactory factory = new StdSchedulerFactory();
            try {
                final boolean createOwnScheduler = factory.getAllSchedulers().size() == 0;
                if (createOwnScheduler) {
                    createOwnScheduler(factory);
                } else {
                    logger.debug("Using existing scheduler instance");
                }
                final Scheduler scheduler = factory.getScheduler();
                if (null != scheduler) {
                    logger.trace("Got scheduler "
                            + getUniqueIdentifier(scheduler.getSchedulerName(), scheduler
                                    .getSchedulerInstanceId()));

                    TestJob.initializeTestJob(scheduler);

                    if (schedulerRef.compareAndSet(null, scheduler)) {
                        if (createOwnScheduler) {
                            logger.debug("Quartz scheduler successfully created. Starting...");
                            try {
                                scheduler.start();
                                logger.debug("Quartz started up successfully");
                            } catch (final SchedulerException e) {
                                logger.error("Scheduler start() failed!: " + e.getMessage(), e);
                            }
                        }
                    } else {
                        logger.warn("Tried to set schedulerRef, expecting null but was already set!");
                    }
                } else {
                    logger.error("factory.getScheduler() returned null!");
                }
            } catch (final SchedulerException e) {
                logger.error("Initializing scheduler failed!: " + e.getMessage(), e);
            }
        }
    }

    /**
     * @param factory
     *        {@link StdSchedulerFactory}
     * @throws SchedulerException
     *         on exception
     */
    private void createOwnScheduler(final StdSchedulerFactory factory) throws SchedulerException {
        logger.debug("Creating new Quartz scheduler...");
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
            logger.debug("Using minimal default properties");
            final Properties props = new Properties();
            props.put(PROP_SCHED_INSTANCE_NAME, schedulerInstanceName);
            props.put(PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
            factory.initialize(props);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see chronos.mbeans.QuartzSchedulerAdapterMBean#shutdown()
     */
    @Override
    public final void shutdown() {
        final Scheduler scheduler = schedulerRef.get();
        if (scheduler != null) {
            try {
                logger.trace("Got scheduler "
                        + getUniqueIdentifier(scheduler.getSchedulerName(), scheduler
                                .getSchedulerInstanceId()));
                TestJob.unschedule(scheduler);
                final String[] jobGroupNames = scheduler.getJobGroupNames();
                if (schedulerInstanceName.equals(scheduler.getSchedulerName())) {
                    if (jobGroupNames.length == 0) {
                        logger.debug("Quartz scheduler shutting down...");
                        try {
                            scheduler.shutdown(true);
                        } catch (final SchedulerException e) {
                            logger
                                    .error("Encountered exception shutting down Quartz: " + e.getMessage(),
                                            e);
                        }
                    } else {
                        logger.debug("Job groups still running "
                                + StringUtils.arrayToCommaDelimitedString(jobGroupNames));
                    }
                } else {
                    logger
                            .trace("Was using an existing scheduler instance, so won't perform actual shutdown.");
                }
            } catch (final SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
            if (!schedulerRef.compareAndSet(scheduler, null)) {
                logger.warn("Tried to set schedulerRef to null but was set to a different value!");
            }
        } else {
            logger.error("schedulerRef.get() returned null!");
        }
    }
}
