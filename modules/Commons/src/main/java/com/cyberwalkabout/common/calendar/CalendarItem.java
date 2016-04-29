package com.cyberwalkabout.common.calendar;

class CalendarItem
{
	private int id;
	private String name;
	private String syncAccountName;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSyncAccountName()
	{
		return syncAccountName;
	}

	public void setSyncAccountName(String syncAccountName)
	{
		this.syncAccountName = syncAccountName;
	}
}

