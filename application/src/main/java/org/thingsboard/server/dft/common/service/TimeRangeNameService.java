package org.thingsboard.server.dft.common.service;

import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.common.dtos.TimeRangeNameDto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// binh dv
@Component
public class TimeRangeNameService {

    // convert time range to List<timeRangeNameDto>
    public List<TimeRangeNameDto> convertTimeRange(long startTs, long endTs){
        List<TimeRangeNameDto> result = new ArrayList<>();

        while(getMidnight(startTs) < endTs){
            TimeRangeNameDto dto = new TimeRangeNameDto(startTs, getMidnight(startTs), convert(startTs));
            result.add(dto);
            startTs = getMidnight(startTs);
        }

        TimeRangeNameDto dto = new TimeRangeNameDto(startTs, endTs, convert(startTs));
        result.add(dto);

        return result;
    }

    // convert timestamp (milliseconds) to day ( 7/11/1997 )
    private String convert(long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        return String.format("%d/%d/%d", mDay, mMonth + 1, mYear);
    }

    // get timestamp of 0h next day in milliseconds:
    private long getMidnight(long timestamp){
        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(timestamp);

        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

}
