package chronos

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

class IndexController {

    def schedulerUniqueId

    def jobGroupNames

    def jobGroups = [:]

    def index = {
        def factory = new StdSchedulerFactory();
        factory.initialize();
        def scheduler = factory.getScheduler();

        schedulerUniqueId = "${scheduler.schedulerName}\$${scheduler.schedulerInstanceId}@${System.identityHashCode(scheduler)}" 
        log.debug "Got scheduler ${schedulerUniqueId}"

        jobGroupNames = scheduler.jobGroupNames
        log.debug "Got ${jobGroupNames.size()} jobs"

        jobGroupNames.each { jobGroupName ->
            log.debug "Job Group: ${jobGroupName}"
            jobGroups[jobGroupName] = scheduler.getJobNames(jobGroupName).inject([:]) { jobs, jobName->
                jobs[jobName] = scheduler.getJobDetail(jobGroupName, jobName)
                jobs
            }
        }

    }
}
