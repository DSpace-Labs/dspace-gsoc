package org.dspace.cis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

/**
 * Corresponding to the table *witness*
 * 
 * @author Administrator
 * 
 */
public class Witness {

	private int witness_id;

	private int time_interval_id;

	private String hashvalue;

	private String hash_algorithm;

	/** Our context */
	private Context ourContext;

	public String getHash_algorithm() {
		return hash_algorithm;
	}

	public void setHash_algorithm(String hash_algorithm) {
		this.hash_algorithm = hash_algorithm;
	}

	public String getHashvalue() {
		return hashvalue;
	}

	public void setHashvalue(String hashvalue) {
		this.hashvalue = hashvalue;
	}

	public int getTime_interval_id() {
		return time_interval_id;
	}

	public void setTime_interval_id(int time_interval_id) {
		this.time_interval_id = time_interval_id;
	}

	public int getWitness_id() {
		return witness_id;
	}

	public void setWitness_id(int witness_id) {
		this.witness_id = witness_id;
	}

	public Context getOurContext() {
		return ourContext;
	}

	public void setOurContext(Context ourContext) {
		this.ourContext = ourContext;
	}

	/**
	 * Write this witness into the database
	 * 
	 * @throws SQLException
	 */
	public void archive() throws SQLException {
		TableRow tR = DatabaseManager.row("witness");
		// tR.setColumn("witness_id", this.witness_id);
		tR.setColumn("time_interval_id", this.time_interval_id);
		tR.setColumn("hashvalue", this.hashvalue);
		tR.setColumn("hash_algorithm", this.hash_algorithm);

		DatabaseManager.insert(ourContext, tR);
	}

	/**
	 * Build the witness value for the given interval
	 * 
	 * @param c
	 *            the Context
	 * @param timeinterval_id
	 * @return
	 * @throws SQLException
	 */
	public Witness buildWithInterval(Context c, int timeinterval_id)
			throws SQLException {

		Witness result = new Witness();

		List<String> hashvalues = hashValuesOfInterval(c, timeinterval_id);

		DigestFactory dF = new DigestFactory();

		String tmpString = witHash(hashvalues, dF);

		result.setHash_algorithm(dF.getPRIMITIVE().toString());
		result.setHashvalue(tmpString);
		result.setTime_interval_id(timeinterval_id);
		result.setOurContext(ourContext);

		return result;
	}

	/**
	 * Generate the witness hash value given a list of items' hash values. It's
	 * finished by a assistant Merkley Tree. In each iteration, a temporary list
	 * holds the nodes of a level, generate the parents' hash values and assign
	 * them to the temporary List. This procedure will run in recursion until
	 * the List just hold one string object, which is the output of this
	 * function.
	 * 
	 * @param hashvalues
	 * @param dF
	 * @return
	 */
	private String witHash(List<String> hashvalues, DigestFactory dF) {
		/** temp values to help traversing the hashvalues */
		String[] tmpArray = (String[]) hashvalues.toArray();
		List<String> tmpList = new ArrayList<String>();
		String tmpString = null;
		while (tmpArray.length != 1) {
			/** if the size of the temp array is an even */
			if (tmpArray.length % 2 == 0) {
				for (int i = 0; i < tmpArray.length / 2; i++) {
					tmpString = dF.digest(tmpArray[2 * i])
							+ dF.digest(tmpArray[2 * i + 1]);
					tmpList.add(dF.digest(tmpString));
				}
				tmpArray = (String[]) tmpList.toArray();
				tmpList.clear();
			} else {
				for (int i = 0; i < (tmpArray.length - 1) / 2; i++) {
					tmpString = dF.digest(tmpArray[2 * i])
							+ dF.digest(tmpArray[2 * i + 1]);
					tmpList.add(dF.digest(tmpString));
				}
				tmpList.add(tmpArray[tmpArray.length - 1]);
				tmpArray = (String[]) tmpList.toArray();
				tmpList.clear();
			}
		}

		tmpString = tmpArray[0];
		return tmpString;
	}

	/**
	 * Retrieve a list of hash values of the items in a given time_interal
	 * 
	 * @param c
	 * @param timeinterval_id
	 * @return
	 * @throws SQLException
	 */
	private List<String> hashValuesOfInterval(Context c, int timeinterval_id)
			throws SQLException {

		/** excute the sql command to get the TableRowIterator as a result */
		String query = "SELECT * FROM hashvalueofitem WHERE time_interval_id= ?";
		TableRowIterator tri = DatabaseManager.queryTable(c, "hashvalueofitem",
				query, timeinterval_id);

		/** hold all the hash values in the time_interval */
		List<String> hashvalues = new ArrayList<String>();
		while (tri.hasNext()) {
			TableRow tR = tri.next();
			String hashvalue = tR.getStringColumn("hashvalue");
			hashvalues.add(hashvalue);
		}
		return hashvalues;
	}

	/**
	 * Generate the assistant hash value for the given time_interval and request
	 * numbers (<code>from</code> and <code>to</code>)
	 * 
	 * @param c
	 * @param timeinterval_id
	 * @param from
	 * @param to
	 * @return
	 * @throws SQLException
	 */
	public String witHash(Context c, int timeinterval_id, int from, int to)
			throws SQLException {

		List<String> hashvalues = hashValuesOfInterval(c, timeinterval_id)
				.subList(from, to);

		DigestFactory dF = new DigestFactory();

		return witHash(hashvalues, dF);
	}

}
