package org.dspace.cis;

import java.util.Date;

/**
 * This class presents a time interval in an hour.
 * 
 * @author Wang Jiahui
 * 
 */
public class TimeInterval
{

    /**
     * The time interval's id.
     */
    private int timeInterval_id;

    /**
     * The *from* time of this interval.
     */
    private Date from;

    /**
     * The *to* time of this interval.
     */
    private Date to;

    /**
     * The constructor
     * @param date the time in this time interval
     */
    public TimeInterval(Date date)
    {
        timeInterval_id = CisUtils.getTimeInterval_id(date);
        from = CisUtils.getFrom(date);
        to = CisUtils.getTo(date);
        // this.ourContext = ourContext;
    }

    /**
     * Get the *from* time of this interval.
     * @return the from time
     */
    public Date getFrom()
    {
        return from;
    }

    /**
     * Set the *from* time of this interval.
     * @param from the from time
     */
    public void setFrom(Date from)
    {
        this.from = from;
    }

    /**
     * Get the time interval's id.
     * @return the time interval's id
     */
    public int getTimeInterval_id()
    {
        return timeInterval_id;
    }

    /**
     * Set the time interval's id.
     * @param timeInterval_id the time interval's id
     */
    public void setTimeInterval_id(int timeInterval_id)
    {
        this.timeInterval_id = timeInterval_id;
    }

    /**
     * Get the *to* time of this interval.
     * @return the to time
     */
    public Date getTo()
    {
        return to;
    }

    /**
     * Set the *to* time of this interval.
     * @param to the to time
     */
    public void setTo(Date to)
    {
        this.to = to;
    }

}
