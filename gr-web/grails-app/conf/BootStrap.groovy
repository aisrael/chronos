import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import chronos.TestJob;

class BootStrap {
    
    def init = { servletContext ->
        environments {
            development {
                // Add a test Quartz Job so we have some stuff to show
                def factory = new StdSchedulerFactory();
                factory.initialize();
                def scheduler = factory.getScheduler();
                TestJob.initializeTestJob scheduler
            }
        }
    }
    def destroy = {
    }
}
