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
 * Created Mar 9, 2010
 */

package chronos.web.controllers;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;

/**
 * @author Alistair A. Israel
 */
@Controller
public class IndexController {

    private static final Log logger = LogFactory.getLog(IndexController.class);

    /**
     * @param model
     *        {@link Model}
     */
    @RequestMapping("/index.html")
    public final void index(final Model model) {
        model.addAttribute("now", new Date());
        try {
            final StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize();
            final Scheduler scheduler = factory.getScheduler();
            model.addAttribute("jobGroups", extractJobGroups(scheduler));
            model.addAttribute("triggerGroups", extractTriggers(scheduler));
        } catch (final SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param scheduler
     *        the {@link SchedulerException}
     * @return {@link SimpleSequence}
     * @throws SchedulerException
     *         on exception
     */
    private SimpleSequence extractJobGroups(final Scheduler scheduler) throws SchedulerException {
        final String[] groupNames = scheduler.getJobGroupNames();
        logger.debug("Got " + groupNames.length + " job group names");
        final SimpleSequence groups = new SimpleSequence(groupNames.length);
        for (final String groupName : groupNames) {
            final String[] jobNames = scheduler.getJobNames(groupName);
            final SimpleSequence jobs = new SimpleSequence(jobNames.length);
            logger.debug("Got " + jobNames.length + " job names under group \"" + groupName + "\"");
            for (final String jobName : jobNames) {
                final JobDetail jobDetail = scheduler.getJobDetail(jobName, groupName);
                final String jobClassName = jobDetail.getJobClass().getName();
                logger.debug(jobName + " (" + jobClassName + ")");
                final SimpleHash job = new SimpleHash();
                job.put("name", jobName);
                job.put("class", jobClassName);
                final Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, groupName);
                final SimpleSequence triggerNames = new SimpleSequence(triggersOfJob.length);
                for (final Trigger trigger : triggersOfJob) {
                    triggerNames.add(trigger.getName());
                }
                job.put("triggerNames", triggerNames);
                jobs.add(job);
            }
            final SimpleHash group = new SimpleHash();
            group.put("name", groupName);
            group.put("jobs", jobs);
            groups.add(group);
        }
        return groups;
    }

    /**
     * @param scheduler
     *        the {@link SchedulerException}
     * @return {@link SimpleSequence}
     * @throws SchedulerException
     *         on exception
     */
    private SimpleSequence extractTriggers(final Scheduler scheduler) throws SchedulerException {
        final String[] groupNames = scheduler.getTriggerGroupNames();
        logger.debug("Got " + groupNames.length + " trigger group names");
        final SimpleSequence groups = new SimpleSequence(groupNames.length);
        for (final String groupName : groupNames) {
            final String[] triggerNames = scheduler.getTriggerNames(groupName);
            logger.debug("Got " + triggerNames.length + " trigger names under group \"" + groupName + "\"");
            final SimpleSequence triggers = new SimpleSequence(triggerNames.length);
            for (final String triggerName : triggerNames) {
                final SimpleHash triggerHash = new SimpleHash();
                final Trigger trigger = scheduler.getTrigger(triggerName, groupName);
                final String triggerClassName = trigger.getClass().getName();
                triggerHash.put("name", triggerName);
                triggerHash.put("class", triggerClassName);
                String description = null;
                if (trigger instanceof SimpleTrigger) {
                    final SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                    final int repeatCount = simpleTrigger.getRepeatCount();
                    description = simpleTrigger.getStartTime().toString();
                    if (repeatCount != 0) {
                        description = description + " repeat ";
                        if (repeatCount == SimpleTrigger.REPEAT_INDEFINITELY) {
                            description = description + "indefinitely";
                        } else {
                            description = description + repeatCount + " times";
                        }
                        description = description + " every " + simpleTrigger.getRepeatInterval() + " ms";
                    }
                } else if (trigger instanceof CronTrigger) {
                    final CronTrigger cronTrigger = (CronTrigger) trigger;
                    description = cronTrigger.getCronExpression();
                }
                if (description != null) {
                    triggerHash.put("description", description);
                }
                logger.debug(triggerName + "(" + triggerClassName + ") : " + description);

                triggers.add(triggerHash);
            }
            final SimpleHash group = new SimpleHash();
            group.put("name", groupName);
            group.put("triggers", triggers);
            groups.add(group);
        }
        return groups;
    }
}
