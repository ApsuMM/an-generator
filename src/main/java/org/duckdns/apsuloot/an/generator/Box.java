package org.duckdns.apsuloot.an.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Box
{

	private List<Activity> activities;
	private Float hours;

	Box(List<Activity> activities, Float hours)
	{
		this.activities = activities;
		if (activities == null)
		{
			this.activities = new ArrayList<Activity>();
		}
		this.hours = hours;
	}

	public List<Activity> getActivities()
	{
		return activities;
	}

	public void addActivity(Activity activity)
	{
		this.activities.add(activity);
	}

	public float getHours()
	{
		return hours;
	}

	public void addHours(Float hours)
	{
		this.hours += hours;
	}

	public String getContent()
	{
		int contentSize = this.activities.stream().reduce(0, (total, act) -> total + act.getContents().size(), Integer::sum);
		int[] lines = { 0 };

		List<String> contentList = new ArrayList<String>();
		this.activities.forEach(act ->
		{
			List<String> contents = act.getContents();
			if (act.getProperties().getRandom())
				Collections.shuffle(contents);

			if (contentSize > 8)
			{
				int share = Math.round((contents.size() / (float) contentSize) * 8);
				if (lines[0] + share > 8)
				{
					share = 8 - lines[0];
				}
				lines[0] += share;

				for (int i = 0; i < share; i++)
				{
					contentList.add(contents.get(i));
				}
			} else
			{
				contentList.addAll(contents);
			}
		});
		return String.join("\u000B", contentList);
	}
}
