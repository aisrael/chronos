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
 * Created Feb 1, 2010
 */
package chronos.web.listener;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * @author Alistair A. Israel
 */
public class TestJob implements StatefulJob {

    /**
     *
     */
    public static final String INITIAL_CONTEXT = InitialContext.class.getName();

    private static final Log logger = LogFactory.getLog(TestJob.class);

    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public final void execute(final JobExecutionContext context) throws JobExecutionException {
        logger
                .info("TestJob@" + System.identityHashCode(this) + " executing "
                        + System.currentTimeMillis());
        final Object obj = context.getJobDetail().getJobDataMap().get(INITIAL_CONTEXT);
        if (obj != null) {
            logger.trace("jobDataMap.get(\"" + INITIAL_CONTEXT + "\") returned " + obj.getClass() + "@"
                    + System.identityHashCode(obj));
            if (obj instanceof Context) {
                executeInJndiContext((Context) obj);
            } else {
                logger.error("jobDataMap.get(\"" + INITIAL_CONTEXT + "\") is of type " + obj.getClass()
                        + ", expecting a " + Context.class + "!");
            }
        } else {
            logger.error("jobDataMap.get(\"" + INITIAL_CONTEXT + "\") returned null!");
        }
    }

    /**
     * @param ic
     *        {@link Context}
     */
    private void executeInJndiContext(final Context ic) {
        try {
            final Object obj = ic.lookup("jdbc/PBR");
            if (obj != null) {
                logger.trace("JNDI lookup of \"jdbc/PBR\" returned " + obj.getClass().getName() + "@"
                        + System.identityHashCode(obj));
                if (obj instanceof DataSource) {
                    logger.trace("Ready to perform JDBC operations...");
                } else {
                    logger.warn("Object at \"jdbc/PBR\" is of type " + obj.getClass()
                            + ", expecting a javax.sql.DataSource!");
                }
            } else {
                logger.warn("JNDI lookup of \"jdbc/PBR\" returned null!");
            }
        } catch (final NamingException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
