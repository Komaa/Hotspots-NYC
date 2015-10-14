package de.tuberlin.iosp.Parser;

import de.tuberlin.iosp.Detector.Config;
import org.codehaus.jettison.json.JSONException;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Koma on 02/06/15.
 */
public class JsonGenerator {

    static double latSpan = 0.004172370000000001 * 4 / 6;
    static double longSpan = 0.00417367910447754 * 4 / 6;
//		double latSpan = 0.004172370000000001 / 2;
//		double longSpan = 0.00417367910447754 / 2;
    // We need to take 5/3 of x and then divide by two to get half

	public static boolean isNumeric (String str) {
		try {
			double d = Double.parseDouble(str);
		} catch ( NumberFormatException nfe ) {
			return false;
		}
		return true;
	}

	public static HashMap<String, Day> readcsvfile (final File folder, ArrayList<String> holidays) throws IOException {
		HashMap<String, Day> days = new HashMap<String, Day>();
		Day tmpday;
		HotSpot tmphotspot;
		HourChunk tmphour;
//        System.out.println(folder.getPath());
		for ( final File fileEntry : folder.listFiles() ) {
			if ( fileEntry.isDirectory() ) {

				readcsvfile(fileEntry, holidays);
			} else {
				if ( isNumeric(fileEntry.getName()) ) {

					BufferedReader br = new BufferedReader(new FileReader(fileEntry));
					String line;
					while ( (line = br.readLine()) != null ) {
						//System.out.println(line);
						String[] lineSplit = line.split(",");
						if ( lineSplit.length > 5 ) {
							tmphotspot = new HotSpot(Double.parseDouble(lineSplit[3]), Double.parseDouble(lineSplit[4]), Integer.parseInt(lineSplit[5]));

							if ( lineSplit.length == 7 ) {
								tmphotspot.setIshotspot(Boolean.parseBoolean(lineSplit[6]));

							}

							if ( !days.containsKey(lineSplit[0]) ) {
								tmpday = new Day(lineSplit[0]);
								if ( holidays.contains(tmpday.getDay()) ) {
									tmpday.setIsHoliday(true);
								}
								tmphour = new HourChunk(lineSplit[1], lineSplit[2]);

								tmphour.getHotspots().add(tmphotspot);
								tmpday.getHourChunkList().add(tmphour);
								days.put(lineSplit[0], tmpday);
							} else {
								tmpday = days.get(lineSplit[0]);
								tmphour = new HourChunk(lineSplit[1], lineSplit[2]);
								if ( !tmpday.getHourChunkList().contains(tmphour) ) {
									tmphour.getHotspots().add(tmphotspot);
									tmpday.getHourChunkList().add(tmphour);
                                    days.replace(lineSplit[0], tmpday);
								} else {
									int index = tmpday.getHourChunkList().indexOf(tmphour);
									tmphour = tmpday.getHourChunkList().get(index);
									tmphour.getHotspots().add(tmphotspot);
									tmpday.getHourChunkList().set(index, tmphour);
								}

							}

						}
					}


				}
			}
		}
		return days;
	}

	public static HashMap<String, Day> readWeatherCSV (final File weatherFile, HashMap<String, Day> days) throws IOException {

		if ( !weatherFile.isFile() ) {
			throw new FileNotFoundException("File does not exist. Please enter a valid file path.");
		}

		BufferedReader br = new BufferedReader(new FileReader(weatherFile));
		String line;
		while ( (line = br.readLine()) != null ) {
			String[] lineSplit = line.split(",");

			// Check if all fields are available
			if ( lineSplit.length == 14 ) {

				/** All Data in weather.csv
				 *
				 * TimeEST
				 * TemperatureC
				 * Dew PointC
				 * Humidity
				 * Sea Level PressurehPa
				 * VisibilityKm
				 * Wind Direction
				 * Wind SpeedKm/h
				 * Gust SpeedKm/h
				 * Precipitationmm
				 * Events
				 * Conditions
				 * WindDirDegrees
				 * DateUTC
				 */

				try {
					// Required Fields. If one of these fails skip it
					Float temperature = Float.parseFloat(lineSplit[1]);
					String weatherEvents = lineSplit[10];
					String conditions = lineSplit[11];
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 2013-01-01 05:51:00
					Date date = formatter.parse(lineSplit[13]);

					Weather weather = new Weather(temperature, weatherEvents, conditions, date, true);

					String day = weather.getYearMonthDayDate();
					if ( days.containsKey(day) ) {
						Day tmpDay = days.get(day);
						List<HourChunk> hourChunks = tmpDay.getHourChunkList();

						for ( HourChunk hourChunk : hourChunks ) {
							if ( JsonGenerator.isHourInRagen(weather.getHourMinuteSecondDate(), hourChunk.getInitial_hour(), hourChunk.getFinal_hour()) ) {
								// Add weather to HourChunk
								hourChunk.addWeather(weather);

								// leave for loop
								break;
							}
						}
					}
				} catch ( NumberFormatException | ParseException e ) {
					// skip header or other malformed values in CSV
				}
			}
		}

		return days;
	}

	public static HashMap<String, Day> readEventsCSV (final File eventsFile, HashMap<String, Day> days) throws IOException {

		if ( !eventsFile.isFile() ) {
			throw new FileNotFoundException("File does not exist. Please enter a valid file path.");
		}

		BufferedReader br = new BufferedReader(new FileReader(eventsFile));
		String line;
		while ( (line = br.readLine()) != null ) {
			String[] lineSplit = line.split(";");

			// Check if all fields are available
			if ( lineSplit.length == 6 ) {

				/** All Data in events.csv
				 *
				 * attending
				 * start_time
				 * end_time
				 * latitude
				 * longitude
				 */

                FbEvent event = new FbEvent(lineSplit[0], lineSplit[1], lineSplit[2], lineSplit[3], lineSplit[4], lineSplit[5]);

                List<String> eventDays = event.getDays();
                for (int i = 0; i < eventDays.size(); i++) {
                    String eventDay = eventDays.get(i);
                    if (days.containsKey(eventDay)) {
                        Day day = days.get(eventDay);
                        List<HourChunk> hourChunks = day.getHourChunkList();

                        for (HourChunk hourChunk : hourChunks) {
                            List<HotSpot> hotspots = hourChunk.getHotspots();
                            try {
                                if (JsonGenerator.isTimeInRagen(event.getStartTime(), event.getEndTime(), eventDay, hourChunk.getInitial_hour(), hourChunk.getFinal_hour())) {
                                    for (HotSpot hotspot : hotspots) {
                                        // Remember: event/hotspot latitudes include negative sign (e.g. -73.99))
                                        // Increase the chunk by 1/3 of its initial size to account for the border-cases
//                                      lat+lat long+long lat-lat long-long (lat/long ~ 0.002)
//                                      -73.971916585 40.785136739552236
//                                      -73.976088955 40.78096306044775
                                        // increase lat long which are dimensions of half of a chunk
                                        if (event.getLatitude() < (hotspot.getLatitude() + latSpan) && event.getLatitude() > (hotspot.getLatitude() - latSpan)) {
                                            if (event.getLongitude() < (hotspot.getLongitude() + longSpan) && event.getLongitude() > (hotspot.getLongitude() - longSpan)) {
                                                hotspot.incrementN_fb_events();
                                                hotspot.incN_fb_people(event.getAttending());
                                            }
                                        }
                                    }
                                    hourChunk.addEvent(event);
                                }
                            } catch (ParseException e) {
                                // skip header or other malformed values in CSV
                            }
                        }
                    }
                }
            }
        }
        return days;
	}

    public static Boolean isTimeInRagen(Date startTime, Date endTime, String eventDay, String beginningHour, String endingHour ) throws ParseException {

        try {
            // startTime and endTime
            Date initHour = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(eventDay+" "+beginningHour);
            Date finalHour = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(eventDay+" "+endingHour);

//            System.out.println("event time is "+startTime+ " - "+endTime);
//            System.out.println("while this chunk is from "+initHour+" to "+finalHour);
            Boolean result = startTime.after(initHour) && startTime.before(finalHour) ||
                    endTime.after(initHour) && endTime.before(finalHour) ||
                    initHour.after(startTime) && finalHour.before(endTime);
//            System.out.println(result);
            return result;
//            return startTime.after(initHour) && startTime.before(finalHour) ||
//                endTime.after(initHour) && endTime.before(finalHour) ||
//                initHour.after(startTime) && endTime.before(finalHour);
        } catch ( ParseException e ) {
            throw new ParseException( "Hours couldn't be parsed to Date Object", e.getErrorOffset() );
        }

    }

	public static Boolean isHourInRagen( String hour, String beginningHour, String endingHour ) throws ParseException {

		try {
			Date checkHour = new SimpleDateFormat("HH:mm").parse(hour);
			Date initHour = new SimpleDateFormat("HH:mm").parse(beginningHour);
			Date finalHour = new SimpleDateFormat("HH:mm").parse(endingHour);

			return checkHour.after(initHour) && checkHour.before(finalHour);
		} catch ( ParseException e ) {
			throw new ParseException( "Hours couldn't be parsed to Date Object", e.getErrorOffset() );
		}

	}

	public static HashMap<String, Day> testdataset (HashMap<String, Day> days, int chunk) {
		int size = days.size();
		List<String> keyList = new ArrayList<String>(days.keySet());
		HashMap<String, Day> randomMap = new HashMap();
		for ( int x = 0; x < (size / chunk) && !keyList.isEmpty(); x++ ) {
			String key = keyList.remove(new Random().nextInt(keyList.size()));
			randomMap.put(key, days.get(key));
		}
		return randomMap;
	}

	public void run() {
		ArrayList<String> holidays = null;
		HashMap<String, Day> dayspickup = null, daysdrop=null, testdatapickup=null, testdatadrop = null;
		String tmp = "";
        int cnt=0;

		final File holidaysFile = new File(Config.getDataPath() + "holiday.txt");
		try {
			holidays = getHolidayList(holidaysFile);
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		final File folderpickup = new File(Config.outputPath() + "Hotspots/Pickup/Pickup");
        final File folderdrop = new File(Config.outputPath() + "Hotspots/Drop/Drop");
		try {
			dayspickup = readcsvfile(folderpickup, holidays);
            daysdrop = readcsvfile(folderdrop, holidays);
			final File weatherFile = new File(Config.pathToWeatherCSV());

			final File eventsFile = new File(Config.pathToEventsCSV());

			dayspickup = readWeatherCSV(weatherFile, dayspickup);
			dayspickup = readEventsCSV(eventsFile, dayspickup);

            daysdrop = readWeatherCSV(weatherFile, daysdrop);
            daysdrop = readEventsCSV(eventsFile, daysdrop);
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		try {
			// Writing to a file
			File file = new File(Config.outputPathjson() + "DayPickUpJSONFile.json");
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			System.out.println("Writing JSON object to file");
			System.out.println("-----------------------");
			fileWriter.write("{\"days\":[");
			for ( Map.Entry<String, Day> entry : dayspickup.entrySet() ) {
                cnt++;
                tmp += entry.getValue().jsonday(false).toString() + ",";
                if(cnt==30){
                    fileWriter.write(tmp);
                    tmp="";
                    cnt=0;
                }
			}
			fileWriter.write(tmp.substring(0, tmp.length() - 2));
			fileWriter.write("}]}");
			tmp = "";
            cnt=0;
			fileWriter.flush();
			fileWriter.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( JSONException e ) {
			e.printStackTrace();
		}

        try {
            // Writing to a file
            File file = new File(Config.outputPathjson() + "DayDropOffJSONFile.json");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("{\"days\":[");
            for ( Map.Entry<String, Day> entry : daysdrop.entrySet() ) {
                tmp += entry.getValue().jsonday(false).toString() + ",";
                cnt++;
                if(cnt==30){
                    fileWriter.write(tmp);
                    tmp="";
                    cnt=0;
                }
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            fileWriter.write("}]}");
            tmp = "";
            cnt=0;
            fileWriter.flush();
            fileWriter.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( JSONException e ) {
            e.printStackTrace();
        }


		final File folder2 = new File(Config.outputPath() + "DatasetPrediction/Pickup/Pickup");
        final File folder2drop = new File(Config.outputPath() + "DatasetPrediction/Drop/Drop");
		try {
			dayspickup = readcsvfile(folder2, holidays);
            daysdrop = readcsvfile(folder2drop, holidays);
			final File weatherFile = new File(Config.pathToWeatherCSV());
            final File eventsFile = new File(Config.pathToEventsCSV());

	        dayspickup = readWeatherCSV(weatherFile, dayspickup);
            dayspickup = readEventsCSV(eventsFile, dayspickup);

            daysdrop = readWeatherCSV(weatherFile, daysdrop);
            daysdrop = readEventsCSV(eventsFile, daysdrop);
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		testdatapickup = testdataset(dayspickup,10);
        testdatadrop = testdataset(daysdrop,10);

        HashSet<String> weathervpickup=getpossibleweather(dayspickup);
        HashSet<String> hourvpickup=getpossiblehour(dayspickup);

        HashSet<String> weathervdrop=getpossibleweather(daysdrop);
        HashSet<String> hourvdrop=getpossiblehour(daysdrop);

        //@relation ads

        // @attribute latitude numeric
        // @attribute longitude numeric
        // @attribute hour {Y, N}
        // @attribute isholiday {true, false}
        // @attribute meantemp numeric
        // @attribute Weather {}
        // @attribute n_fb_events numeric
        // @attribute n_fb_people numeric
        // @attribute n_people numeric

        //@data

		try {
            System.out.println("Writing Day Regression to file");
			// Writing to a file
			File file = new File(Config.outputPathjson() + "DaysPickUpRegression.txt");
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("@relation ads\n\n");
            fileWriter.write("@attribute latitude numeric\n");
            fileWriter.write("@attribute longitude numeric\n");
            fileWriter.write("@attribute hour {");
            tmp="";
            for ( String hour : hourvpickup ) {
                tmp+=hour+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            fileWriter.write("}\n");
            fileWriter.write("@attribute isholiday {true, false}\n");
            fileWriter.write("@attribute meantemp numeric\n");
            fileWriter.write("@attribute weather {");
            tmp="";
            for ( String weather : weathervpickup ) {
                tmp+=weather.replaceAll("\\s+","")+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            tmp="";
            fileWriter.write("}\n");
            fileWriter.write("@attribute n_fb_events numeric\n");
            fileWriter.write("@attribute n_fb_people numeric\n");
            fileWriter.write("@attribute n_people numeric\n\n");
            fileWriter.write("@data\n");
            for ( Map.Entry<String, Day> entry : testdatapickup.entrySet() ) {
                tmp += entry.getValue().csvday(true)+"\n";
				//System.out.println(entry.getValue().jsonday());
                cnt++;
                if(cnt==30){
                    fileWriter.write(tmp);
                    tmp="";
                    cnt=0;
                }
			}

            fileWriter.write(tmp);
            cnt=0;

			fileWriter.flush();
			fileWriter.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( JSONException e ) {
			e.printStackTrace();
		}


        try {

            // Writing to a file
            File file = new File(Config.outputPathjson() + "DaysDropOffRegression.txt");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("@relation ads\n\n");
            fileWriter.write("@attribute latitude numeric\n");
            fileWriter.write("@attribute longitude numeric\n");
            fileWriter.write("@attribute hour {");
            tmp="";
            for ( String hour : hourvdrop ) {
                tmp+=hour+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            fileWriter.write("}\n");
            fileWriter.write("@attribute isholiday {true, false}\n");
            fileWriter.write("@attribute meantemp numeric\n");
            fileWriter.write("@attribute weather {");
            tmp="";
            for ( String weather : weathervdrop ) {
                tmp+=weather.replaceAll("\\s+","")+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            tmp="";
            fileWriter.write("}\n");
            fileWriter.write("@attribute n_fb_events numeric\n");
            fileWriter.write("@attribute n_fb_people numeric\n");
            fileWriter.write("@attribute n_people numeric\n\n");
            fileWriter.write("@data\n");
            for ( Map.Entry<String, Day> entry : testdatadrop.entrySet() ) {
                tmp += entry.getValue().csvday(true)+"\n";
                //System.out.println(entry.getValue().jsonday());
                cnt++;
                if(cnt==30){
                    fileWriter.write(tmp);
                    tmp="";
                    cnt=0;
                }
            }

            fileWriter.write(tmp);
            tmp = "";

            fileWriter.flush();
            fileWriter.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( JSONException e ) {
            e.printStackTrace();
        }


        //@relation ads

        // @attribute latitude numeric
        // @attribute longitude numeric
        // @attribute hour {Y, N}
        // @attribute isholiday {true, false}
        // @attribute meantemp numeric
        // @attribute Weather {}
        // @attribute n_fb_events numeric
        // @attribute n_fb_people numeric
        // @attribute ishotspot {true, false}

        //@data

        try {
            // Writing to a file
            System.out.println("Writing Day Classification to file");
            File file = new File(Config.outputPathjson() + "DaysPickUpClassification.txt");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("@relation ads\n\n");
            fileWriter.write("@attribute latitude numeric\n");
            fileWriter.write("@attribute longitude numeric\n");
            fileWriter.write("@attribute hour {");
            tmp="";
            for ( String hour : hourvpickup ) {
                tmp+=hour+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            fileWriter.write("}\n");
            fileWriter.write("@attribute isholiday {true, false}\n");
            fileWriter.write("@attribute meantemp numeric\n");
            fileWriter.write("@attribute weather {");
            tmp="";
            for ( String weather : weathervpickup ) {
                tmp+=weather.replaceAll("\\s+","")+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            tmp="";
            fileWriter.write("}\n");
            fileWriter.write("@attribute n_fb_events numeric\n");
            fileWriter.write("@attribute n_fb_people numeric\n");
            fileWriter.write("@attribute ishotspot {true, false}\n\n");
            fileWriter.write("@data\n");
            for ( Map.Entry<String, Day> entry : testdatapickup.entrySet() ) {
                tmp += entry.getValue().csvday(false)+"\n";
                //System.out.println(entry.getValue().jsonday());
                cnt++;
                if(cnt==100){
                    fileWriter.write(tmp);
                    tmp="";
                    cnt=0;
                }
            }

            fileWriter.write(tmp);
            tmp = "";

            fileWriter.flush();
            fileWriter.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( JSONException e ) {
            e.printStackTrace();
        }

        try {
            // Writing to a file

            File file = new File(Config.outputPathjson() + "DaysDropOffClassification.txt");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("@relation ads\n\n");
            fileWriter.write("@attribute latitude numeric\n");
            fileWriter.write("@attribute longitude numeric\n");
            fileWriter.write("@attribute hour {");
            tmp="";
            for ( String hour : hourvdrop ) {
                tmp+=hour+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            fileWriter.write("}\n");
            fileWriter.write("@attribute isholiday {true, false}\n");
            fileWriter.write("@attribute meantemp numeric\n");
            fileWriter.write("@attribute weather {");
            tmp="";
            for ( String weather : weathervdrop ) {
                tmp+=weather.replaceAll("\\s+","")+", ";
            }
            fileWriter.write(tmp.substring(0, tmp.length() - 2));
            tmp="";
            fileWriter.write("}\n");
            fileWriter.write("@attribute n_fb_events numeric\n");
            fileWriter.write("@attribute n_fb_people numeric\n");
            fileWriter.write("@attribute ishotspot {true, false}\n\n");
            fileWriter.write("@data\n");
            for ( Map.Entry<String, Day> entry : testdatadrop.entrySet() ) {
                tmp += entry.getValue().csvday(false)+"\n";
                //System.out.println(entry.getValue().jsonday());
                cnt++;
                if(cnt==100){
                    fileWriter.write(tmp);
                    tmp="";
                    cnt=0;
                }
            }

            fileWriter.write(tmp);
            tmp = "";

            fileWriter.flush();
            fileWriter.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( JSONException e ) {
            e.printStackTrace();
        }

		System.out.println("JSON Files written to: " + Config.outputPath());
	}

    private static HashSet<String> getpossiblehour(HashMap<String, Day> days) {
        HashSet<String> possiblehour=new HashSet<String>();
        for ( Map.Entry<String, Day> entry : days.entrySet() ) {
            List<HourChunk> tmp= entry.getValue().getHourChunkList();
            for (HourChunk temp : tmp) {
               possiblehour.add(temp.getInitial_hour());
            }
        }
        return possiblehour;
    }

    private static HashSet<String> getpossibleweather(HashMap<String, Day> days) {
        HashSet<String> possibleweather=new HashSet<String>();
        for ( Map.Entry<String, Day> entry : days.entrySet() ) {
            List<HourChunk> tmp= entry.getValue().getHourChunkList();
            for (HourChunk temp : tmp) {
                for(Weather weathe : temp.getWeatherList())
                    possibleweather.add(weathe.getConditions());
            }
        }
        return possibleweather;
    }

    private static ArrayList<String> getHolidayList (File holidaysFile) throws IOException {
		ArrayList<String> holidays = new ArrayList<String>();

		if ( ! holidaysFile.isFile() ) {
			throw new FileNotFoundException("Holiday file does not exist. Please enter a valid file path.");
		}

		BufferedReader br = new BufferedReader(new FileReader(holidaysFile));
		String line;
		while ( (line = br.readLine()) != null ) {
			holidays.add(line);
		}

		return holidays;
	}

}
