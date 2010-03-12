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

import static chronos.utils.time.TimeConstants.HOURS_PER_DAY;
import static chronos.utils.time.TimeConstants.MILLIS_PER_SECOND;
import static chronos.utils.time.TimeConstants.MINUTES_PER_HOUR;
import static chronos.utils.time.TimeConstants.SECONDS_PER_MINUTE;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeUnit;

/**
 * @author Alistair A. Israel
 */
public class Period {

    private final long value;

    private final TimeUnit unit;

    /**
     * @param value
     *        the quantity
     * @param unit
     *        the {@link UnitOfTime}
     */
    public Period(final long value, final TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * @param seconds
     *        the number of seconds
     * @return {@link Period}
     */
    public static Period seconds(final long seconds) {
        return new Period(seconds, SECONDS);
    }

    /**
     * @param minutes
     *        the number of minutes
     * @return {@link Period}
     */
    public static Period minutes(final long minutes) {
        return new Period(minutes, MINUTES);
    }

    /**
     * @param hours
     *        the number of hours
     * @return {@link Period}
     */
    public static Period hours(final long hours) {
        return new Period(hours, HOURS);
    }

    /**
     * @param days
     *        the number of days
     * @return {@link Period}
     */
    public static Period days(final long days) {
        return new Period(days, DAYS);
    }

    /**
     * @return the value
     */
    public final long getValue() {
        return value;
    }

    /**
     * @return the unit
     */
    public final TimeUnit getUnit() {
        return unit;
    }

    /**
     * @return this period in milliseconds
     */
    public final long inMillis() {
        return unit.toMillis(value);
    }

    /**
     * @param millis
     *        the number of milliseconds
     * @return a 'human' readable period
     */
    public static Period humanize(final long millis) {
        if (millis % MILLIS_PER_SECOND == 0) {
            final long seconds = millis / MILLIS_PER_SECOND;
            if (seconds % SECONDS_PER_MINUTE == 0) {
                final long minutes = seconds / SECONDS_PER_MINUTE;
                if (minutes % MINUTES_PER_HOUR == 0) {
                    final long hours = minutes / MINUTES_PER_HOUR;
                    if (hours % HOURS_PER_DAY == 0) {
                        return days(hours / HOURS_PER_DAY);
                    } else {
                        return hours(hours);
                    }
                } else {
                    return minutes(minutes);
                }
            } else {
                return seconds(seconds);
            }
        } else {
            return new Period(millis, MILLISECONDS);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    // CHECKSTYLE:OFF
    @Override
    public String toString() {
        // CHECKSTYLE:ON
        return Long.toString(value) + " " + unit;
    }
}
