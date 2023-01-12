package org.duckdns.apsuloot.an.generator;

public class LinkedActivity
{

	private Activity activity;
	private Float hours = null;

	LinkedActivity(Activity activity)
	{
		this.setActivity(activity);
	}

	LinkedActivity(Activity activity, Float hours)
	{
		this.setActivity(activity);
		this.setHours(hours);
	}

	public Activity getActivity()
	{
		return activity;
	}

	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}

	public Float getHours()
	{
		return hours;
	}

	public void setHours(Float hours)
	{
		this.hours = hours;
	}
}
