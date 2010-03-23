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
package chronos;

import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * @author Alistair A. Israel
 */
public class TestJob implements StatefulJob {
    
    /**
     * 
     */
    public static final String CHRONOS = "Chronos";

    /**
     *
     */
    public static final String TEST_JOB_NAME = "TestJob";

    /**
     *
     */
    public static final String INITIAL_CONTEXT = InitialContext.class.getName();

    private static final Log logger = LogFactory.getLog(TestJob.class);

    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public final void execute(final JobExecutionContext context) throws JobExecutionException {
        logger
                .info("TestJob@" + System.identityHashCode(this) + " executing "
                        + System.currentTimeMillis());
        final Object obj = context.getJobDetail().getJobDataMap().get(INITIAL_CONTEXT);
        if (obj != null) {
            logger.trace("jobDataMap.get(\"" + INITIAL_CONTEXT + "\") returned " + obj.getClass() + "@"
                    + System.identityHashCode(obj));
            if (obj instanceof Context) {
                executeInJndiContext((Context) obj);
            } else {
                logger.error("jobDataMap.get(\"" + INITIAL_CONTEXT + "\") is of type " + obj.getClass()
                        + ", expecting a " + Context.class + "!");
            }
        } else {
            logger.error("jobDataMap.get(\"" + INITIAL_CONTEXT + "\") returned null!");
        }
    }

    /**
     * @param ic
     *        {@link Context}
     */
    private void executeInJndiContext(final Context ic) {
        // noop
    }

    /**
     * @param scheduler
     *        the {@link Scheduler}
     * @throws SchedulerException
     *         on exception
     */
    public static final void initializeTestJob(final Scheduler scheduler) throws SchedulerException {
        try {
            final Class<?> clazz = Class.forName(TestJob.class.getName());
            final JobDetail jobDetail = new JobDetail(TEST_JOB_NAME, CHRONOS, clazz);
            final JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(InitialContext.class.getName(), new InitialContext());

            final Trigger trigger = TriggerUtils.makeMinutelyTrigger();
            trigger.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));
            trigger.setName("TestTrigger");

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (final NamingException e) {
            logger.error("JNDI Exception: " + e.getMessage(), e);
        } catch (final ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param scheduler
     *        the {@link Scheduler}
     */
    public static void unschedule(final Scheduler scheduler) {
        try {
            final Trigger[] triggersOfJob = scheduler.getTriggersOfJob(TEST_JOB_NAME, CHRONOS);
            for (final Trigger trigger : triggersOfJob) {
                logger.debug("Unscheduling trigger " + trigger.getFullName() + " ("
                        + trigger.getFullJobName() + ")");
                scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
            }
        } catch (final SchedulerException e) {
            logger.error("SchedulerException unscheduling " + TEST_JOB_NAME + ": " + e.getMessage(), e);
        }
    }
}
