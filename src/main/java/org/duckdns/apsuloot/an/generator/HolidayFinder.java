package org.duckdns.apsuloot.an.generator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import kong.unirest.Unirest;

public class HolidayFinder
{

	private static final HolidayFinder finder = new HolidayFinder();
	private List<LocalDate> holidaysCache = new ArrayList<LocalDate>();
	private List<Integer> cachedYears = new ArrayList<Integer>();

	private HolidayFinder()
	{
	}

	public static HolidayFinder getInstance()
	{
		return finder;
	}

	public Boolean isHoliday(LocalDate date) throws IOException
	{
		int year = date.getYear();

		if (!this.cachedYears.contains(year))
		{
			JSONArray jar = new JSONArray(Unirest
				.get(String.format("https://date.nager.at/api/v3/PublicHolidays/%s/DE", year))
				.asString()
				.getBody());

			for (int i = 0; jar.length() > i; i++)
			{
				JSONObject jobj = (JSONObject) jar.get(i);
				if ((Boolean) jobj.get("global") == true)
				{
					this.addHoliday(jobj.getString("date"));
					continue;
				}

				if (jobj.has("counties"))
				{
					jobj.getJSONArray("counties").forEach(s ->
					{
						if (((String) s).contains("DE-MV"))
						{
							this.addHoliday(jobj.getString("date"));
						}
					});
				}
			}
			this.cachedYears.add(year);
		}
		return this.holidaysCache.contains(date);
	}

	public void addHoliday(String date)
	{
		String[] dateStringArray = date.split("-");
		holidaysCache.add(LocalDate.of(Integer.valueOf(dateStringArray[0]),
			Integer.valueOf(dateStringArray[1]), Integer.valueOf(dateStringArray[2])));
	}
}
