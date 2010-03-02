/**
 *
 */
package chronos.mbeans;

/**
 * @author Alistair A. Israel
 */
public interface QuartzSchedulerAdapterMBean {

    /**
     * Returns the version of Quartz. Similar to calling
     * {@link org.quartz.core.QuartzScheduler#getVersion()}.
     *
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
