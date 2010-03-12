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
 * Created Mar 12, 2010
 */
package chronos.test;

import static javax.management.ObjectName.WILDCARD;

import java.util.ArrayList;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.junit.Test;

/**
 * @author Alistair A. Israel
 */
public final class JmxTest {

    /**
     * @throws Exception
     *         on exception
     */
    @Test
    public void testJmx() throws Exception {
        final MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer("Test");
        System.out.println(mBeanServer.getDefaultDomain());

        final Set<ObjectName> names = mBeanServer.queryNames(WILDCARD, null);
        for (final ObjectName name : names) {
            System.out.println(name);
        }

        final ObjectName mbsd = new ObjectName("*", "type", "MBeanServerDelegate");
        final Set<ObjectName> found = mBeanServer.queryNames(mbsd, null);
        System.out.println("Found " + found.size() + " MBeans");
        final ObjectName name = found.iterator().next();
        System.out.println(mBeanServer.getAttribute(name, "MBeanServerId"));

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
