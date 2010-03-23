import org.quartz.SimpleTrigger;

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
                log.debug "Job ${jobGroupName}.${jobName}..."
                def jobDetail = scheduler.getJobDetail(jobName, jobGroupName)
                def triggers = scheduler.getTriggersOfJob(jobName, jobGroupName).collect { trigger ->
                    def className = trigger.getClass().getName()
                    def tmap = [ name : trigger.name,
                      className : className ]
                    if (trigger instanceof SimpleTrigger) { 
                        def simpleTrigger = (SimpleTrigger) trigger;
                        def repeatCount = simpleTrigger.repeatCount;
                        def description = "${simpleTrigger.startTime}";
                        if (repeatCount != 0) {
                            description = description + " repeat ";
                            if (repeatCount == SimpleTrigger.REPEAT_INDEFINITELY) {
                                description = description + "indefinitely";
                            } else {
                                description = description + "${repeatCount } times";
                            }
                            description = description + " every ${simpleTrigger.repeatInterval} ms";
                        }
                        tmap.description = description
                        log.debug "${trigger.name} ${className}: ${description}"
                    }
                    tmap
                }
                jobs[jobName] = [name : jobName, className : jobDetail.jobClass.name, triggers : triggers]
                jobs
            }
        }

    }
}
