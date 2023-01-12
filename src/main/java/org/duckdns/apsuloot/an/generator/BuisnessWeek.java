package org.duckdns.apsuloot.an.generator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;

public class BuisnessWeek {

    private LocalDate start;
    private LocalDate end;
    private int year;
    private int hours;
    private int weekOfYear;

    BuisnessWeek(LocalDate start, LocalDate end)
    {
        this.start = start;
        this.end = end;
        this.year = start.getYear();
        this.initHours();

        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        calendar.set(start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth());
        this.weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
    }

    BuisnessWeek(LocalDate start, LocalDate end, int weekOfyear, int hours)
    {
        this.start = start;
        this.end = end;
        this.weekOfYear = weekOfyear;
        this.hours = hours;
    }

    public LocalDate getStart()
    {
        return start;
    }

    public LocalDate getEnd()
    {
        return end;
    }

    public int getWeekOfYear()
    {
        return weekOfYear;
    }

    public int getYear()
    {
        return year;
    }

    public int getHours()
    {
        return hours;
    }

    private void initHours()
    {
        HolidayFinder finder = HolidayFinder.getInstance();
        long holidays = start.datesUntil(end.plusDays(1)).filter(d -> {
            try
            {
                return finder.isHoliday(d);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                System.out.println("Exception related to holiday API");
            }
            return false;
        }).count();
        this.hours = (int) (40 - holidays * 8);
    }
}
