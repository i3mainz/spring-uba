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

public class DateCreator {

    public ZonedDateTime create(Object measurementStamp) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeStamp;

        if (measurementStamp == null) {
            return createZonedDateTime(now);
        }

        if (measurementStamp instanceof Date) {
            return createZonedDateTime(LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(((Date) measurementStamp).getTime()), ZoneId.systemDefault()));
        }

        if (measurementStamp instanceof String) {

            String pattern = "(\\+|\\-)(\\d+)(H|D)$";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(measurementStamp.toString());

            if (m.find()) {
                timeStamp = now.plus(new Long(m.group(1) + m.group(2)),
                        m.group(3).equals("D") ? ChronoUnit.DAYS : ChronoUnit.HOURS);
            } else {
                try {
                    timeStamp = LocalDateTime.parse(measurementStamp.toString());
                } catch (DateTimeParseException e) {
                    return createZonedDateTime(now);
                }
            }
            return createZonedDateTime(timeStamp != null ? timeStamp : now);
        }

        return createZonedDateTime(now);
    }

    /**
     * @param now
     * @return
     */
    private ZonedDateTime createZonedDateTime(LocalDateTime now) {
        return ZonedDateTime.of(resetTime(now), ZoneId.of("Europe/Berlin"));
    }

    private LocalDateTime resetTime(LocalDateTime timeStamp) {
        return timeStamp.withMinute(0).withSecond(0).withNano(0);
    }

}
