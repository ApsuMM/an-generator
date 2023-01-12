package org.duckdns.apsuloot.an.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileParser
{

	private static final FileParser fileParser = new FileParser();

	private FileParser()
	{
	}

	public static FileParser getInstance()
	{
		return fileParser;
	}

	public List<Activity> readActivities() throws IOException
	{
		List<Activity> activities = new ArrayList<Activity>();
		File file = new File(this.getClass().getResource("/activities.txt").getPath());
		List<String> cleanLines = FileParser.removeComments(Files.readAllLines(file.toPath()));

		for (int i = 0; cleanLines.size() > i; i++)
		{
			if (cleanLines.get(i).trim().matches("\\[.+?\\]"))
			{
				String name = cleanLines.get(i).trim().replaceAll("\\[", "").replaceAll("\\]", "");
				ActivityProperties properties = new ActivityProperties();
				List<String> content = new ArrayList<String>();

				i++;
				while (!(cleanLines.get(i).isEmpty() || cleanLines.get(i).isBlank()))
				{
					if (cleanLines.get(i).matches("\\:.+?\\=.+?"))
					{
						String[] stringParts = cleanLines.get(i).replaceFirst(":", "").split("=");
						switch (stringParts[0])
						{
						case "visible":
							properties.setVisibility(Boolean.parseBoolean(stringParts[1]));
							break;
						case "random":
							properties.setRandom(Boolean.parseBoolean(stringParts[1]));
							break;
						case "box":
							properties.setBox(Integer.valueOf(stringParts[1]));
							break;
						}
						i++;
						if (i == cleanLines.size())
							break;

						continue;
					}
					content.add(cleanLines.get(i));
					i++;

					if (i == cleanLines.size())
						break;
				}
				activities.add(new Activity(name, content, properties));
			}
		}
		return activities;
	}

	public List<DocumentJob> readJobs(File file, List<Activity> availableActivities) throws IOException
	{
		List<DocumentJob> jobs = new ArrayList<DocumentJob>();
		List<String> cleanLines = FileParser.removeComments(Files.readAllLines(file.toPath()));

		for (int i = 0; cleanLines.size() > i; i++)
		{
			if (!cleanLines.get(i)
				.matches(
					"WEEK\\:\\d{0,2}?\\sSTART\\:\\d{4}-\\d{2}-\\d{2}\\sEND\\:\\d{4}-\\d{2}-\\d{2}\\sHOURS\\:\\d{0,2}?.*?"))
			{
				continue;
			}

			String[] lineStringParts = cleanLines.get(i).split("\\s", 5);
			int week = Integer.valueOf(lineStringParts[0].split("\\:")[1]);
			int hours = Integer.valueOf(lineStringParts[3].split("\\:")[1]);

			String[] startDateParts = lineStringParts[1].split("\\:")[1].split("-");
			LocalDate start = LocalDate.of(Integer.valueOf(startDateParts[0]),
				Integer.valueOf(startDateParts[1]), Integer.valueOf(startDateParts[2]));

			String[] endDateParts = lineStringParts[2].split("\\:")[1].split("-");
			LocalDate end = LocalDate.of(Integer.valueOf(endDateParts[0]),
				Integer.valueOf(endDateParts[1]), Integer.valueOf(endDateParts[2]));

//            List<Activity> jobActivities = new ArrayList<Activity>();
			List<LinkedActivity> linkedActivities = new ArrayList<LinkedActivity>();
			String[] activitiesParts = lineStringParts[4].split("\\:", 2)[1].split(",");
			for (int j = 0; activitiesParts.length > j; j++)
			{
				int var = j;
				Optional<Activity> val = availableActivities.stream()
					.filter(act -> act.getName().contentEquals(activitiesParts[var].trim().replaceAll("\\[(.*?)\\]", "")))
					.findFirst();

				if (val.isPresent())
				{
//                    jobActivities.add(availableActivities.stream()
//                            .filter(act -> act.getName().contentEquals(activitiesParts[var].trim().replaceAll("\\[(.*?)\\]", "")))
//                            .toList()
//                            .get(0));

					Activity activity = availableActivities.stream()
						.filter(act -> act.getName().contentEquals(activitiesParts[var].trim().replaceAll("\\[(.*?)\\]", "")))
						.toList()
						.get(0);

					Float activityHours = null;
					if (activitiesParts[var].matches(".+?\\[(.*?)\\]"))
					{
						activityHours = Float.valueOf(activitiesParts[var].replaceAll(".+?\\[|\\]", ""));
					}
					linkedActivities.add(new LinkedActivity(activity, activityHours));
				}
			}
			jobs.add(new DocumentJob(start, end, week, hours, linkedActivities));
		}
		return jobs;
	}

	private static List<String> removeComments(List<String> lines)
	{
		List<String> cleanLines = new ArrayList<String>();
		lines.forEach(line ->
		{
			if (line.length() > 0)
			{
				line = removeClosingCommentChars(line);
			}
			cleanLines.add(line.split("#")[0]);
		});
		return cleanLines;
	}

	private static String removeClosingCommentChars(String line)
	{
		String cleanLine = line;

		if (line.charAt(line.length() - 1) == '#')
		{
			cleanLine = line.substring(0, line.length() - 1);

			if (cleanLine.length() > 0)
				removeClosingCommentChars(cleanLine);
		}
		return cleanLine;
	}
}
