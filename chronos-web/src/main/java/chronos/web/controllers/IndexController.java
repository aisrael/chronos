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
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
        try {
            final StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize();
            final Scheduler scheduler = factory.getScheduler();
            final String[] groupNames = scheduler.getJobGroupNames();
            logger.debug("Got " + groupNames.length + " job group names");
            for (final String groupName : groupNames) {
                final String[] jobNames = scheduler.getJobNames(groupName);
                logger.debug("Got " + jobNames.length + " job names under group \"" + groupName + "\"");
                for (final String jobName : jobNames) {
                    final JobDetail jobDetail = scheduler.getJobDetail(jobName, groupName);
                    logger.debug(jobName + " (" + jobDetail.getJobClass().getName() + ")");
                }
            }
        } catch (final SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        model.addAttribute("now", new Date());
    }
}