package org.dspace.cis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 * a Certificate class
 * @author Jiahui Wang
 *
 */
public class Certificate
{
    private String algorithm;
    private String handle;
    private List witnesses;
    public Date from;
    public Date to;
    
    public Certificate()
    {
//        setInterval();
    	witnesses = new ArrayList();
    }
    
    public String getAlgorithm()
    {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm)
    {
        this.algorithm = algorithm;
    }
    
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    
    public String getHandle()
    {
        return handle;
    }
    
    public void addWitness(Witness witness){
        witnesses.add(witness);
    }
    
    public Witness[] getWitnesses()
    {
        return (Witness[])witnesses.toArray();
    }
    
/**
 * The interval would be set to an hour 
 * Both <code>from</code> and <code>to<code> are set by current time
 *
 */
    private void setInterval()
    {
        Calendar c = Calendar.getInstance(); 
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        
        c.set(year, month, date, hour, 0, 0);
        Date from = c.getTime();
        
        long miliSeconds = from.getTime();
        miliSeconds += 3600000; 
      
        Date to = new Date(miliSeconds);
        
        this.from = from;
        this.to = to;
    }
//    public static void main(String[] argv)
//    {
//        Calendar c = Calendar.getInstance(); 
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
//        int date = c.get(Calendar.DAY_OF_MONTH);
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        
//        c.set(year, month, date, hour, 0, 0);
//        Date from = c.getTime();
//        
//        long miliSeconds = from.getTime();
//        miliSeconds += 3600000; 
//      
//        Date to = new Date(miliSeconds);
//        
//        System.out.println(from);
//        System.out.println(to);
//    }

}
