package org.dspace.statistics.dao;
import org.dspace.core.Context;

/**
 * Factory class that provides DAO classes
 *
 * @author Federico Paparoni
 */

public class StatisticsDAOFactory {
	public static SearchEventDAO getSearchEventDAO(Context context)
    {
            return new SearchEventDAO(context);
    }

	public static AuthenticationEventDAO getAuthenticationEventDAO(Context context)
    {
            return new AuthenticationEventDAO(context);
    }

	public static ContentEventDAO getContentEventDAO(Context context)
    {
            return new ContentEventDAO(context);
    }
}
