package org.dspace.cis;

import java.util.Date;

public class TimeInterval
{   
    // 271752 is the number of hours from 00:00:00 CST 1970 to 00:00:00 CST
    // 2001
    private static final int HOURS_OFFSET = 271752;

    //the number of milliseconds of an hour
    private static final int MILLISECONDS_OF_AN_HOUR = 60 * 60 * 1000;

    private int timeInterval_id;

    private Date from;

    private Date to;


    public TimeInterval(Date date)
    {
        timeInterval_id = getTimeInterval_id(date);
        from = getFrom(date);
        to = getTo(date);
    }

//    public static void main(String[] args)
//    {
//        Calendar c = Calendar.getInstance();
//        c.set(2001, Calendar.JANUARY, 1, 0, 0, 0);
//        Date date1 = c.getTime();
//        System.out.println(date1);
//        Date now = new Date();
//        Date date2 = getFrom(now);
//        Date date3 = getTo(now);
//        System.out.println(now);
//        System.out.println(date2);
//        System.out.println(date3);
//        long longvalue = now.getTime() / MILLISECONDS_OF_AN_HOUR;
//        long inter = date1.getTime() / MILLISECONDS_OF_AN_HOUR;
//        Long l = new Long(longvalue);
//        int intvalue = l.intValue();
//        Integer i = new Integer(intvalue);
//        System.out.println(inter);
//        System.out.println(l);
//        System.out.println(longvalue);
//        System.out.println(intvalue);
//        System.out.println(i);
//    }

    // the timeInterval_id is number of hours since Mon Jan 01 00:00:00 CST 2001
    public static int getTimeInterval_id(Date date)
    {
        Long tmp = new Long(date.getTime());
        
        tmp = tmp / MILLISECONDS_OF_AN_HOUR - HOURS_OFFSET;
        
        int result;
        result = (new Long(tmp)).intValue();
        return result;
    }
    //get from time of the time-interval of a given time
    public static Date getFrom(Date date)
    {
       
        long milliseds = date.getTime();
        long tmp = milliseds % (MILLISECONDS_OF_AN_HOUR);
        milliseds = milliseds - tmp;
        Date result = new Date(milliseds);
        return result;
    }
    //get to time of the time-interval of a given time
    public static Date getTo(Date date)
    {
        long milliseds = date.getTime();
        long tmp = milliseds % (MILLISECONDS_OF_AN_HOUR);
        milliseds = milliseds - tmp + MILLISECONDS_OF_AN_HOUR;
        Date result = new Date(milliseds);
        return result;
    }

    public Date getFrom()
    {
        return from;
    }

    public void setFrom(Date from)
    {
        this.from = from;
    }

    public int getTimeInterval_id()
    {
        return timeInterval_id;
    }

    public void setTimeInterval_id(int timeInterval_id)
    {
        this.timeInterval_id = timeInterval_id;
    }

    public Date getTo()
    {
        return to;
    }

    public void setTo(Date to)
    {
        this.to = to;
    }
}
