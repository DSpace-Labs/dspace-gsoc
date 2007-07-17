package org.dspace.statistics.dao;
import org.dspace.core.Context;

public class StatisticsDAOFactory {
	public static SearchItemDAO getSearchItemDAOFactory(Context context)
    {
            return new SearchItemDAOImpl(context);
    }
}
