package com.timestored.cstore;



/** 
 * Represents a single column in a QTable. Where it's a simple vector all
 * of the same type.
 */
public interface CColumn {

	/** 
	 * @return true if this is a key column, otheriwse false.
	 */
	public boolean isKey();
	
	public CAtomTypes getType();
	
	public String getTitle();
	
	public Object getValues();
}
