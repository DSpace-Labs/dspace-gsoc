package org.dspace.cis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A Certificate should like this:
 * <code>[t; (y4, R), (H12, L), (W{i-1}, L)]</code> where <code>t</code>
 * indicates the time when item is submitted to the repository
 * <code>(y4, R)</code>, <code>(H12, L)</code> and <code>(W{i-1}, L)</code>
 * are all assitant hash values to generate the putative witness the putative
 * witness will be compared to the published witness to determine whether the
 * certificate is validate
 * <p>
 * A assistant hash contains two parts: the hashvalue and the position
 * information (which could be L(left) or R(right)). The hashvalue could be a
 * single Item's(like <code>y4</code>), a catenated hash value(like
 * <code>H12</code>) or a witness (like <code>W{i-1}</code>)
 * 
 * @author Jiahui Wang
 * 
 */
public class Certificate {
	private String algorithm;

	private String handle;

	private List<AssistHash> witnesses;

	public Date from;

	public Date to;

	public Certificate() {
		// setInterval();
		witnesses = new ArrayList<AssistHash>();
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getHandle() {
		return handle;
	}

	public void addWitness(AssistHash witness) {
		witnesses.add(witness);
	}

	public AssistHash[] getWitnesses() {
		return (AssistHash[]) witnesses.toArray();
	}

	/**
	 * The interval would be set to an hour 
	 * Both <code>from</code> and <code>to<code> are set by current time
	 *
	 */
	private void setInterval() {
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
	// public static void main(String[] argv)
	// {
	// Calendar c = Calendar.getInstance();
	// int year = c.get(Calendar.YEAR);
	// int month = c.get(Calendar.MONTH);
	// int date = c.get(Calendar.DAY_OF_MONTH);
	// int hour = c.get(Calendar.HOUR_OF_DAY);
	//        
	// c.set(year, month, date, hour, 0, 0);
	// Date from = c.getTime();
	//        
	// long miliSeconds = from.getTime();
	// miliSeconds += 3600000;
	//      
	// Date to = new Date(miliSeconds);
	//        
	// System.out.println(from);
	// System.out.println(to);
	// }

}
