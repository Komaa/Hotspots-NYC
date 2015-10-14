package de.tuberlin.iosp.Detector;

public class Config {


	private Config () {
	}


	public static String basePath () { return System.getProperty("user.dir"); }

	public static String getDataPath () { return Config.basePath() + "/data/"; }

	public static String pathToInput () { return Config.getDataPath() + "trip/trip_data_1.csv"; }

	public static String pathToWeatherCSV () { return Config.getDataPath() + "weather.csv"; }

	public static String pathToEventsCSV () { return Config.getDataPath() + "events.csv"; }

	public static String outputPath () {
		return Config.basePath() + "/output/";
	}

	public static String outputPathjson () {
		return Config.basePath() + "/output/";
	}

	public static int hourchunk () {
		return 24;
	}

	public static int citychunklong () {
		return 134;
	}

	public static int citychunklat () {
		return 100;
	}

//eastLimit= -73.699959, westLimit= -74.259232, southLimit= 40.490984, nordLimit= 40.908221;

	public static double eastLimit () {
		return -73.699959;
	}

	public static double westLimit () {
		return -74.259232;
	}

	public static double southLimit () {
		return 40.490984;
	}

	public static double nordLimit () {
		return 40.908221;
	}

}