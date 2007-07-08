package org.dspace.cis;

//import java.sql.SQLException;
import java.util.Date;
//
//import org.dspace.core.Context;
//import org.dspace.storage.rdbms.*;

public class TimeInterval
{   

//    private static final int MILLISECONDS_OF_AN_HOUR = 60 * 60 * 1000;

    private int timeInterval_id;

    private Date from;

    private Date to;
    
//    /** Our context */
//    private Context ourContext;
    
//    /** The table row corresponding to this timeInterval */
//    private TableRow intervalRow;
    
//    public TimeInterval(TableRow intervalRow, Context ourContext)
//    {
//      timeInterval_id = intervalRow.getIntColumn("time_interval_id");
//      from = intervalRow.getDateColumn("from");
//      to = intervalRow.getDateColumn("to");
//      this.intervalRow = intervalRow;
//      this.ourContext = ourContext;
//    }
    public TimeInterval(Date date/*, Context ourContext*/)
    {
        timeInterval_id = Utils.getTimeInterval_id(date);
        from = Utils.getFrom(date);
        to = Utils.getTo(date);
//        this.ourContext = ourContext;
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
//      TimeInterval tI = new TimeInterval(new Date());
//      try {
//          tI.archive();
//      } catch (SQLException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//      
//    }



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
    
//    public void archive() throws SQLException
//    {
////        List<String> collums = new ArrayList<String>();
////        collums.add("time_interval_id");
////        collums.add("from");
////        collums.add("to");
//      
////        TableRow tR = new TableRow("timeinterval", collums);
//      TableRow tR = DatabaseManager.row("timeinterval");
//      tR.setColumn("time_interval_id", this.timeInterval_id);
//      tR.setColumn("from", this.from);
//      tR.setColumn("to", this.to);
//      
//      DatabaseManager.insert(ourContext, tR);
//    }
}
