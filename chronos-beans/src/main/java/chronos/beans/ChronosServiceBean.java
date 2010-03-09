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
 * Created Mar 2, 2010
 */
package chronos.beans;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chronos.ChronosService;

/**
 * @author Alistair A. Israel
 */
@Stateless(mappedName = ChronosService.JNDI_NAME)
public class ChronosServiceBean implements ChronosService {

    private static final Log logger = LogFactory.getLog(ChronosServiceBean.class);

    /**
     * Called by the J2EE container after EJB construction.
     */
    @PostConstruct
    public final void postConstruct() {
        logger.debug("ChronosServiceBean@" + System.identityHashCode(this) + "#postConstruct()...");
    }
}
