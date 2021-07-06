package org.thingsboard.server.dft.common.service;

import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.common.dtos.TimeRange;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class TimeQueryService {

  public List<TimeRange> getTimeRangeQuery(long startTime, long endTime) {
    List<TimeRange> timeRangeList = new ArrayList<>();
    long trueEndTime = endTime;
//    endTime = removeSecondInTime(endTime);
    if((endTime - startTime) <= 600000) {
      TimeRange timeRange = new TimeRange();
      timeRange.setStartTime(startTime);
      timeRange.setEndTime(endTime);
      timeRangeList.add(timeRange);
      return timeRangeList;
    }
    Calendar calendarStartTime = GregorianCalendar.getInstance();
    calendarStartTime.setTimeInMillis(startTime);
    if (calendarStartTime.get(Calendar.MINUTE) % 10 != 0) {
      int firstStep = 10 - calendarStartTime.get(Calendar.MINUTE) % 10;
      TimeRange timeRange = new TimeRange();
      timeRange.setStartTime(startTime);
      startTime += firstStep * 60000;
      timeRange.setEndTime(startTime);
      timeRangeList.add(timeRange);
    }
    while (startTime < (endTime - Long.parseLong("600000"))) {
      TimeRange timeRange = new TimeRange();
      timeRange.setStartTime(startTime);
      startTime += Long.parseLong("600000");
      timeRange.setEndTime(startTime);
      timeRangeList.add(timeRange);
    }
    if(startTime != endTime) {
      TimeRange timeRange = new TimeRange();
      timeRange.setStartTime(startTime);
      timeRange.setEndTime(trueEndTime);
      timeRangeList.add(timeRange);
    }
    return timeRangeList;
  }

  public String getStringRangeTime(TimeRange timeRange) {
    Calendar startTime = GregorianCalendar.getInstance();
    startTime.setTimeInMillis(timeRange.getStartTime());
    Calendar endTime = GregorianCalendar.getInstance();
    endTime.setTimeInMillis(timeRange.getEndTime());

    SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-YYYY");
    Date date = startTime.getTime();

    StringBuilder timeRangeString = new StringBuilder();
    timeRangeString.append(formatDate.format(date));
    timeRangeString.append(" ");
    timeRangeString.append(
        endTime.get(Calendar.HOUR_OF_DAY)
            + ":"
            + (endTime.get(Calendar.MINUTE) == 0 ? "00" : createMinString(endTime.get(Calendar.MINUTE))));
    timeRangeString.append("-");
    timeRangeString.append(
        startTime.get(Calendar.HOUR_OF_DAY)
            + ":"
            + (startTime.get(Calendar.MINUTE) == 0 ? "00" : createMinString(startTime.get(Calendar.MINUTE))));
    return timeRangeString.toString();
  }

  private String createMinString(int min) {
    if(0 < min && min < 9) {
      return "0" + String.valueOf(min);
    }
    return String.valueOf(min);
  }

  public List<TimeRange> getDayRangeQuery(long startTime, long endTime) {
    startTime = toStartDay(startTime);
    endTime = toEndDay(endTime);
    List<TimeRange> timeRangeList = new ArrayList<>();
    if((endTime - startTime) <= 86400000) {
      TimeRange timeRange = new TimeRange();
      timeRange.setStartTime(startTime);
      timeRange.setEndTime(endTime);
      timeRangeList.add(timeRange);
      return timeRangeList;
    }
    while (startTime < endTime) {
      TimeRange timeRange = new TimeRange();
      timeRange.setStartTime(startTime);
      startTime += Long.parseLong("86400000");
      timeRange.setEndTime(startTime-1);
      timeRangeList.add(timeRange);
    }
    return timeRangeList;
  }

  public String getDateByTimestamp(long timestamp) {
    Calendar timeCalendar = GregorianCalendar.getInstance();
    timeCalendar.setTimeInMillis(timestamp);
    SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-YYYY");
    Date date = timeCalendar.getTime();

    StringBuilder timeRangeString = new StringBuilder();
    timeRangeString.append(formatDate.format(date));
    return timeRangeString.toString();
  }

  public long toStartDay(long timestamp) {
    LocalDateTime utcDate =
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    long startDayTs = Timestamp.valueOf(utcDate.toLocalDate().atTime(LocalTime.MIN)).getTime();
    return startDayTs;
  }

  public long toEndDay(long timestamp) {
    LocalDateTime utcDate =
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    long endDayTs = Timestamp.valueOf(utcDate.toLocalDate().atTime(LocalTime.MAX)).getTime();
    return endDayTs;
  }
}
