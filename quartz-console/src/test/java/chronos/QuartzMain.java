/**
 *
 */
package chronos;

import java.util.Collection;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Alistair A. Israel
 */
public class QuartzMain {

    /**
     * @param args
     *        String[]
     * @throws Exception
     *         on exception
     */
    @SuppressWarnings("unchecked")
    public static void main(final String[] args) throws Exception {
        final StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize();
        final Collection<Scheduler> allSchedulers = factory.getAllSchedulers();
        System.out.println("Got " + allSchedulers.size() + " schedulers...");
    }
}
