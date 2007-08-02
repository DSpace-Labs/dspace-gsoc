package org.dspace.statistics.dao;
import org.dspace.core.Context;

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
