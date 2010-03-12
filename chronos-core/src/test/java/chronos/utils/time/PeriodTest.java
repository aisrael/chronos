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
package chronos.utils.time;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * JUnit test for {@link Period}.
 *
 * @author Alistair A. Israel
 */
public final class PeriodTest {

    /**
     * Test method for {@link Period#humanize(long)}.
     */
    @Test
    public void testHumanize() {
        final long[] expectedValues = {
                12345678,
                11,
                61,
                25,
                1
        };
        final TimeUnit[] expectedUnits = {
                MILLISECONDS,
                SECONDS,
                MINUTES,
                HOURS,
                DAYS
        };
        for (int i = 0; i < expectedValues.length; ++i) {
            final long millis = expectedUnits[i].toMillis(expectedValues[i]);
            final Period period = Period.humanize(millis);
            assertEquals(expectedValues[i], period.getValue());
            assertEquals(expectedUnits[i], period.getUnit());
        }
    }

}
