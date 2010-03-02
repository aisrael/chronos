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
     *
     */
    void start();

    /**
     *
     */
    void shutdown();

}
