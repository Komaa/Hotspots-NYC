package de.tuberlin.iosp.Parser;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Koma on 02/06/15.
 */
public class FbEvent {

    Calendar cal = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    private Integer attending;
    private BigInteger idevent;
    private Date startTime;
    private Date endTime;
    private Date startDay;
    private Date endDay;
    private Float latitude;
    private Float longitude;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatterDay = new SimpleDateFormat("yyyy-MM-dd");

    public Integer getAttending() {
        return attending;
    }

    public void setAttending(Integer attending) {
        this.attending = attending;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public Float getLongitude() {
        return longitude;
    }

    public long getSpan() {
        long diff = this.endTime.getTime() - this.startTime.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)+1;
    }

    public List<String> getDays() {
        List<String> days = new ArrayList<String>();
        cal.setTime(this.startDay); // Get today's date as a Calendar
        cal2.setTime(this.endDay);
        while (!cal.after(cal2)) {
            days.add(formatterDay.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
            // do not add days in 2014...
        }
//        days.add(formatter.format(cal.getTime()));
        return days;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public FbEvent(String attending, String startDate, String endDate, String latitude, String longitude, String idevent) {

        this.attending = Integer.parseInt(attending);
        this.latitude = Float.parseFloat(longitude);
        this.longitude = Float.parseFloat(latitude);

        this.idevent = new BigInteger(idevent);

        // Destroy the event if it takes more than 10 days (?)

        try {
            this.startTime = formatter.parse(startDate);
            this.startDay = formatterDay.parse(startDate.split(" ")[0]);
            if (!endDate.equals("\"N")) {
                this.endTime = formatter.parse(endDate);
                this.endDay  = formatterDay.parse(endDate.split(" ")[0]);
            } else {
                this.endTime = this.startTime;
                this.endDay  = this.startDay;
                // Make them equal - assume event takes one hour on a given day (?)
            }
        } catch (ParseException e) {
            System.out.println("Could not create an Event. Error while parsing Date.");
        }

    }

    public JSONObject toJson() throws JSONException {

        JSONObject obj = new JSONObject();
//        obj.put("StartDate", this.getStartDate());
//        obj.put("EndDate", this.getEndDate());
        obj.put("Latitude", this.getLatitude());
        obj.put("Longitude", this.getLongitude());
        obj.put("Attending", this.getAttending());
        obj.put("id", this.getIdevent());
//        obj.put("Attending", this.getTemperature());
//        obj.put("StartDate", this.getWeatherEvents());
//        obj.put("endT", this.getConditions());
//        obj.put("Date", this.getYearMonthDayDate() + " " + this.getHourMinuteSecondDate());
        return obj;
    }

    public BigInteger getIdevent() {
        return idevent;
    }

    public void setIdevent(BigInteger idevent) {
        this.idevent = idevent;
    }
}
