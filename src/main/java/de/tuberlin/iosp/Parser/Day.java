package de.tuberlin.iosp.Parser;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Koma on 02/06/15.
 */
public class Day {
    List<HourChunk> hourChunkList;
    String day;
    Boolean isHoliday;

    public Day(String day) {
        this.day = day;
        this.hourChunkList=new ArrayList<HourChunk>();
        this.setIsHoliday(false); // assume it is not a holiday
    }

    public List<HourChunk> getHourChunkList() {
        return hourChunkList;
    }

    public void setHourChunkList(List<HourChunk> hourChunkList) {
        this.hourChunkList = hourChunkList;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Boolean getIsHoliday () {
        return isHoliday;
    }

    public void setIsHoliday (Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Day day1 = (Day) o;

        if (!day.equals(day1.day)) return false;
        if (hourChunkList != null ? !hourChunkList.equals(day1.hourChunkList) : day1.hourChunkList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return day.hashCode();
    }

    public JSONObject jsonday( Boolean isTSet ) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("day",day);
        obj.put("isHoliday", this.getIsHoliday());


        JSONArray listhour = new JSONArray();
        Iterator<HourChunk> iter=hourChunkList.iterator();

        while (iter.hasNext()) {
            listhour.put(iter.next().jsonhour( isTSet ));
        }
        obj.put("Hour",listhour);

        return obj;
    }

    public String csvday(boolean flag) throws JSONException {
        String csvstring="";


        //latitude, longitude, hour, type_day, n_of_events, n_people_attending, Weather=yes

        Iterator<HourChunk> iter=hourChunkList.iterator();

        while (iter.hasNext()) {
            csvstring+=iter.next().csvhour(getIsHoliday(),flag);
        }
        return csvstring;
    }
}
