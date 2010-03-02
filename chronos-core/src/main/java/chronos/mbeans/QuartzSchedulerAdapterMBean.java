/**
 *
 */
package chronos.mbeans;

/**
 * @author Alistair A. Israel
 */
public interface QuartzSchedulerAdapterMBean {

    /**
     * @return the quartz version
     */
    String getQuartzVersion();

    /**
     * @return {@code true} if Quartz is running
     */
    boolean isQuartzRunning();

    /**
     *
     */
    void start();

    /**
     *
     */
    void shutdown();

}
