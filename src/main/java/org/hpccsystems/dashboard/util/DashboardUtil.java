package org.hpccsystems.dashboard.util;

public class DashboardUtil {
	private static final long serialVersionUID = 1L;	
	/**
	 * Checks whether a column is numeric
	 * @param column
	 * @param dataType
	 * @return
	 */
	public static boolean checkNumeric(final String dataType)
	{
		boolean numericColumn = false;
			if(dataType.contains("integer")	|| 
					dataType.contains("real") || 
					dataType.contains("decimal") ||  
					dataType.contains("unsigned"))	{
				numericColumn = true;
			}
		return numericColumn;
	}

}
