/**
 *
 */
package chronos.test;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.junit.Test;

/**
 * @author Alistair A. Israel
 */
public class JmxTest {

    @Test
    public void testJmx() {
        final MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer();

        final ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        System.out.println("Got " + servers.size() + " servers");
        for (final MBeanServer server : servers) {
            System.out.println(server.getDefaultDomain());
            final String[] domains = server.getDomains();
            System.out.println("Got " + domains.length + " domains");
            for (final String domain : domains) {
                System.out.println(domain);
            }
        }
    }

}
