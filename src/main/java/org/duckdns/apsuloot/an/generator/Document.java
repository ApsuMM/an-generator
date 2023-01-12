package org.duckdns.apsuloot.an.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

public class Document
{
	DocumentJob job;
	User user;
	Box box1 = new Box(null, (float) 0.0);
	Box box2 = new Box(null, (float) 0.0);
	Box box3 = new Box(null, (float) 0.0);

	Document(DocumentJob job, User user)
	{
		this.job = job;
		this.user = user;
		job.getLinkedActivities().forEach(linkAct ->
		{
//			linkAct.getActivity().getName().replaceAll(".+?\\[", "");
			switch (String.valueOf(linkAct.getActivity().getProperties().getBox()))
			{
			case "1":
				box1.addActivity(linkAct.getActivity());
				box1.addHours(linkAct.getHours() == null ? 0 : linkAct.getHours());
				break;
			case "2":
				box2.addActivity(linkAct.getActivity());
				box2.addHours(linkAct.getHours() == null ? 0 : linkAct.getHours());
				break;
			case "3":
				box3.addActivity(linkAct.getActivity());
				box3.addHours(linkAct.getHours() == null ? 0 : linkAct.getHours());
				break;
			}
		});

		if (job.getLinkedActivities().size() == 1 && job.getLinkedActivities().get(0).getHours() == null)
		{
			switch (String.valueOf(job.getLinkedActivities().get(0).getActivity().getProperties().getBox()))
			{
			case "1":
				box1.addHours((float) job.getHours());
				break;
			case "2":
				box2.addHours((float) job.getHours());
				break;
			case "3":
				box3.addHours((float) job.getHours());
				break;
			}
		}

		float totalHours = box1.getHours() + box2.getHours() + box3.getHours();
		float hours = totalHours < job.getHours() ? job.getHours() - totalHours : 0;
		if (!box2.getActivities().isEmpty())
			box2.addHours(hours);
	}

	File createReportDocumentFile()
	{
		String separator = System.getProperty("file.separator");
		String outputDir = System.getProperty("java.io.tmpdir") + separator + "an-reports";
		new File(outputDir).mkdirs();

		String name = "AN-(" + job.getStart().toString() + ")" + " KW-" + job.getWeekOfYear() + ".doc";
		String filename = outputDir + separator + name;

		File file = new File(filename);
		if (file.exists())
		{
			file.delete();
		}

		try
		{
			Path path = Files.copy(new File(this.getClass().getResource("/template.doc").getFile()).toPath(), Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
			HWPFDocument doc = new HWPFDocument(new FileInputStream(path.toAbsolutePath().toString()));

			Range range = doc.getRange();
			for (int i = 0; i < range.numParagraphs(); i++)
			{
				Paragraph p = range.getParagraph(i);
				for (int j = 0; j < p.numCharacterRuns(); j++)
				{
					CharacterRun r = p.getCharacterRun(j);
					if (r.text().matches("\\Q$(\\E.+?\\)"))
					{
						switch (r.text().replaceAll("\\Q$(\\E|\\)", ""))
						{
						case "nameHead":
							r.replaceText(String.format("%s, %s", this.user.name(), this.user.givenName()), false);
							break;
						case "address":
							r.replaceText(this.user.address(), false);
							break;
						case "jobDescription":
							r.replaceText(this.user.jobDescription(), false);
							break;
						case "specialization":
							r.replaceText(this.user.specialization(), false);
							break;
						case "company":
							r.replaceText(this.user.company(), false);
							break;
						case "instructor":
							r.replaceText(this.user.instructor(), false);
							break;
						case "trainingBegin":
							r.replaceText(this.user.trainingBegin(), false);
							break;
						case "trainingEnd":
							r.replaceText(this.user.trainingEnd(), false);
							break;
						case "name":
							r.replaceText(String.format("%s %s", this.user.givenName(), this.user.name()), false);
							break;
						case "begin":
							r.replaceText(job.getStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), false);
							break;
						case "end":
							r.replaceText(job.getEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), false);
							break;
						case "trainingYear":
							int givenTrainingYear = Integer.valueOf(this.user.trainingYear());
							int currentYear = LocalDate.now().getYear();
							LocalDate date = LocalDate.of(currentYear, 8, 1);

							for (int k = 0; this.job.getStart().isBefore(date.minusYears(k)); k++)
							{
								givenTrainingYear--;
							}
							givenTrainingYear = givenTrainingYear < 1 ? 1 : givenTrainingYear;
							r.replaceText(String.valueOf(givenTrainingYear), false);
							break;
						case "name2":
							r.replaceText(this.job.getLinkedActivities()
								.stream()
								.filter(linkAct -> linkAct.getActivity().getProperties().getVisibility())
								.map(linkAct -> linkAct.getActivity().getName())
								.collect(Collectors.joining(", ")), false);
							break;
						case "box1":
							r.replaceText(this.box1.getContent(), false);
							break;
						case "box2":
							r.replaceText(this.box2.getContent(), false);
							break;
						case "box3":
							r.replaceText(this.box3.getContent(), false);
							break;
						case "hour1":
							r.replaceText(String.valueOf(this.box1.getHours()), false);
							break;
						case "hour2":
							r.replaceText(String.valueOf(this.box2.getHours()), false);
							break;
						case "hour3":
							r.replaceText(String.valueOf(this.box3.getHours()), false);
							break;
						case "currentDate":
							r.replaceText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), false);
						}
					}
				}
			}
			doc.write(path.toFile());
			doc.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.fillInStackTrace();
		}
		return new File(filename);
	}
}
