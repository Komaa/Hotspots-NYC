package de.tuberlin.iosp.Parser;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by RetinaObst on 12.06.15.
 */
public class Weather {

	private Float temperature;
	private String weatherEvents;
	private String conditions;
	private Date date;

	Weather (Float temperature, String weatherEvents, String conditions, Date date, Boolean isUTC) {
		this.setTemperature(temperature);
		this.setWeatherEvents(weatherEvents);
		this.setConditions(conditions);
		this.setDate(date, isUTC);
	}

	public String getConditions () {
		return conditions;
	}

	public void setConditions (String conditions) {
		this.conditions = conditions;
	}

	public Date getDate () {
		return date;
	}

	public void setDate (Date date, Boolean isUTC) {
		if ( isUTC ) {
			Calendar cal = Calendar.getInstance(); // creates calendar
			cal.setTime(date); // sets calendar time/date
			cal.add(Calendar.HOUR_OF_DAY, -5); // adds one hour
			this.date = cal.getTime(); // returns new date object, one hour in the future
		} else {
			this.date = date;
		}
	}

	public String getWeatherEvents () {
		return weatherEvents;
	}

	public void setWeatherEvents (String weatherEvents) {
		this.weatherEvents = weatherEvents;
	}

	public Float getTemperature () {
		return temperature;
	}

	public void setTemperature (Float temperature) {
		this.temperature = temperature;
	}

	public String getYearMonthDayDate() {
		return new SimpleDateFormat("yyyy-MM-dd").format(this.date);
	}

	public String getHourMinuteSecondDate() {
		return new SimpleDateFormat("HH:mm:ss").format(this.date);
	}

	public JSONObject toJson () throws JSONException {
		JSONObject obj = new JSONObject();

		obj.put("Temperature", this.getTemperature());
		obj.put("Events", this.getWeatherEvents());
		obj.put("Conditions", this.getConditions());
		obj.put("Date", this.getYearMonthDayDate() + " " + this.getHourMinuteSecondDate());

		return obj;
	}
}
