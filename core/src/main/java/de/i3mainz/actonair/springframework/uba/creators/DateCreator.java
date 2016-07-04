package de.i3mainz.actonair.springframework.uba.creators;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateCreator {

    private static final Logger LOG = LoggerFactory.getLogger(DateCreator.class);

    public ZonedDateTime create(Object measurementStamp) {
        LocalDateTime now = LocalDateTime.now();

        if (measurementStamp == null) {
            return createZonedDateTime(now);
        }

        if (measurementStamp instanceof Date) {
            return createZonedDateTime(LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(((Date) measurementStamp).getTime()), ZoneId.systemDefault()));
        }

        if (measurementStamp instanceof String) {

            return createDateTimeFromString(measurementStamp, now);
        }

        return createZonedDateTime(now);
    }

    /**
     * @param measurementStamp
     * @param now
     * @param timeStamp
     * @return
     */
    private static ZonedDateTime createDateTimeFromString(Object measurementStamp, LocalDateTime now) {
        LocalDateTime timeStamp;
        String pattern = "(\\+|\\-)(\\d+)(H|D)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(measurementStamp.toString());

        if (m.find()) {
            timeStamp = now.plus(new Long(m.group(1) + m.group(2)),
                    ("D").equals(m.group(3)) ? ChronoUnit.DAYS : ChronoUnit.HOURS);
        } else {
            try {
                timeStamp = LocalDateTime.parse(measurementStamp.toString());
            } catch (DateTimeParseException e) {
                LOG.warn("No datetime information. Set now");
                return createZonedDateTime(now);
            }
        }
        return createZonedDateTime(timeStamp != null ? timeStamp : now);
    }

    /**
     * @param now
     * @return
     */
    private static ZonedDateTime createZonedDateTime(LocalDateTime now) {
        return ZonedDateTime.of(resetTime(now), ZoneId.of("Europe/Berlin"));
    }

    private static LocalDateTime resetTime(LocalDateTime timeStamp) {
        return timeStamp.withMinute(0).withSecond(0).withNano(0);
    }

}
