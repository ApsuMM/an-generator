package org.duckdns.apsuloot.an.generator;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "an-generator", mixinStandardHelpOptions = true, version = "an-generator 1.0.0", description = "Ein Programm, welches die Erstellung von IHK Ausbildungsnachweisen erleichtern soll.\r\n")
public class Main implements Callable<Integer>
{
	private static User user;

	@Option(names = "--jobfile", description = "Die Job Datei, welche für die Dokumentenerstellung genutzt werden soll\n")
	private File file;

	@Option(names = { "--start" }, description = {
		"Ein beliebiges Datum der Startwoche in folgendem Format: \r\n\r\n\t dd.MM.yyyy \r\n\r\n Muss in Verbindung mit --end benutzt werden und erstellt eine Jobdatei im Temporären Verzeichnis des Systems\n" })
	private String start;

	@Option(names = { "--end" }, description = {
		"Ein beliebiges Datum der Endwoche in folgendem Format: \r\n\r\n\t dd.MM.yyyy \r\n\r\n Muss in Verbindung mit --start benutzt werden und erstellt eine Jobdatei im Temporären Verzeichnis des Systems\n" })
	private String end;

	@Option(names = { "-c", "--config" }, description = { "Öffnet die Konfigurationsdatei in welcher die Tätigkeiten definiert werden können\n" })
	boolean config;

	@Override
	public Integer call() throws Exception
	{

		System.out.printf("                                                   _\r\n"
			+ "  __ _ _ __         __ _  ___ _ __   ___ _ __ __ _| |_ ___  _ __\r\n"
			+ " / _` | '_ \\ _____ / _` |/ _ \\ '_ \\ / _ \\ '__/ _` | __/ _ \\| '__|\r\n"
			+ "| (_| | | | |_____| (_| |  __/ | | |  __/ | | (_| | || (_) | |\r\n"
			+ " \\__,_|_| |_|      \\__, |\\___|_| |_|\\___|_|  \\__,_|\\__\\___/|_|\r\n"
			+ "                   |___/\r\nan-generator %s [built on %s]\r\n", this.getBuildInfo("version"), this.getBuildInfo("build.date"));

		String width = " / _` | '_ \\ _____ / _` |/ _ \\ '_ \\ / _ \\ '__/ _` | __/ _ \\| '__|";
		for (int i = 0; i < width.length(); i++)
		{
			System.out.print("-");
		}
		System.out.print("\r\n");

		final File activitiesFile = new File(this.getClass().getResource("/activities.txt").getPath());
		final String separator = System.getProperty("file.separator");
		final String targetFilePath = String.format("%s%s.an-generator%sactivities.txt", System.getProperty("user.home"), separator, separator);
		final File targetFile = new File(targetFilePath);

		if (!targetFile.exists())
		{
			new File(targetFile.getParent()).mkdirs();
			Files.copy(activitiesFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		if (config)
		{
			System.out.println("Pfad zur Konfigurationsdatei:\r\n\r\n\t" + targetFile.getAbsolutePath() + "\r\n");
			Desktop.getDesktop().open(targetFile);
		}

		if (start != null && end != null)
		{
			LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
			LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
			System.out.println("Pfad zur Jobdatei:\r\n\r\n\t" + this.createJobsFile(startDate, endDate).getAbsolutePath() + "\r\n");
		}

		if (file != null)
		{
			System.out.println("Folgende Dokumente konnten erstellt werden:\r\n");
			this.readJobFile(file);
		}
		return 0;
	}

	public static void main(String... args) throws IOException
	{
		// TODO Auto-generated method stub
		// Desktop.getDesktop().open(createJobsFile());
		int exitCode = new CommandLine(new Main()).execute(args);
		System.exit(exitCode);
	}

	public File createJobsFile(LocalDate startDate, LocalDate endDate) throws IOException
	{
		LocalDate start = null;
		for (int i = 0; i < startDate.getDayOfWeek().getValue(); i++)
		{
			start = LocalDate.of(startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth() - i);
		}

		LocalDate end = null;
		for (int i = 0; i < endDate.getDayOfWeek().getValue(); i++)
		{
			end = LocalDate.of(endDate.getYear(), endDate.getMonthValue(), endDate.getDayOfMonth() - i);
		}
		end = end.plusDays(4);

		List<BuisnessWeek> buisnessWeeks = new ArrayList<BuisnessWeek>();
		List<LocalDate> mondays = start.datesUntil(end.plusDays(1)).filter(day -> day.getDayOfWeek().equals(DayOfWeek.MONDAY)).toList();
		List<LocalDate> fridays = start.datesUntil(end.plusDays(1)).filter(day -> day.getDayOfWeek().equals(DayOfWeek.FRIDAY)).toList();

		for (int i = 0; mondays.size() > i; i++)
		{
			buisnessWeeks.add(new BuisnessWeek(mondays.get(i), fridays.get(i)));
		}

		String tmpdir = System.getProperty("java.io.tmpdir");
		String seperator = FileSystems.getDefault().getSeparator();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		File file = new File(String.format("%s%sjobs-file-%s.txt", tmpdir, seperator, timestamp.toString().replaceAll(":", "-")));
		file.createNewFile();

		PrintWriter out = new PrintWriter(file);
		buisnessWeeks.forEach(week -> out.printf("WEEK:%s START:%s END:%s HOURS:%s DEPARTMENT:%n", week.getWeekOfYear(), week.getStart().toString(), week.getEnd().toString(), week.getHours()));
		out.printf("TOTAL: %s WEEKS", buisnessWeeks.size());
		out.close();

		return file;
	}

	public void readJobFile(File file) throws IOException
	{
		String separator = System.getProperty("file.separator");
		String userProperties = String.format("%s%s.an-generator%suser.properties", System.getProperty("user.home"), separator, separator);
		new File(new File(userProperties).getParent()).mkdirs();
		new File(userProperties).createNewFile();

		try (FileInputStream input = new FileInputStream(new File(userProperties)))
		{
			Properties properties = new Properties();
			properties.load(input);
			String name = Main.setProperty("name", properties, "Nachname");
			String givenName = Main.setProperty("givenName", properties, "Vorname");
			String address = Main.setProperty("address", properties, "Adresse");
			String jobDescription = Main.setProperty("jobDescription", properties, "Ausbildungsberuf");
			String specialization = Main.setProperty("specialization", properties, "Fachrichtung/Schwerpunkt");
			String company = Main.setProperty("company", properties, "Ausbildungsbetrieb");
			String instructor = Main.setProperty("instructor", properties, "Ausbilder");
			String trainingBegin = Main.setProperty("trainingBegin", properties, "Beginn der Ausbildung (dd.MM.yyyy)");
			String trainingEnd = Main.setProperty("trainingEnd", properties, "Ende der Ausbildung (dd.MM.yyyy)");
			String trainingYear = Main.setProperty("trainingYear", properties, "Ausbildungsjahr");

			user = new User(name, givenName, address, jobDescription, specialization, company, instructor, trainingBegin, trainingEnd, trainingYear);
		}

		try (OutputStream output = new FileOutputStream(userProperties))
		{
			Properties properties = new Properties();
			properties.setProperty("name", user.name());
			properties.setProperty("givenName", user.givenName());
			properties.setProperty("company", user.company());
			properties.setProperty("address", user.address());
			properties.setProperty("jobDescription", user.jobDescription());
			properties.setProperty("specialization", user.specialization());
			properties.setProperty("instructor", user.instructor());
			properties.setProperty("trainingBegin", user.trainingBegin());
			properties.setProperty("trainingEnd", user.trainingEnd());
			properties.setProperty("trainingYear", user.trainingYear());
			properties.store(output, null);
		}

		FileParser parser = FileParser.getInstance();
		List<Activity> availableActivities = parser.readActivities();
//		parser.readJobs(new File("C:\\Users\\magnus.mahnert\\Desktop\\layoutfile-2022-10-03 15-35-18.369.txt"), availableActivities).forEach(job ->
		parser.readJobs(file, availableActivities).forEach(job ->
		{
			System.out.println(new Document(job, user).createReportDocumentFile().getAbsoluteFile().toString());
		});
	}

	public static String setProperty(String name, Properties properties, String msg)
	{
		if (properties.containsKey(name))
		{
			return properties.getProperty(name);
		}
		Scanner s = new Scanner(System.in);
		System.out.print(msg + ": ");
		String text = s.nextLine();
		return text;
	}

	public String getBuildInfo(String keyname) throws IOException
	{
		try (InputStream input = this.getClass().getResourceAsStream("/version.txt"))
		{
			Properties properties = new Properties();
			properties.load(input);
			return properties.getProperty(keyname);
		}
	}
}
