package de.tuberlin.iosp.Parser;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Koma on 02/06/15.
 */
public class HourChunk {
    List<FbEvent> event;
    List<HotSpot> hotspots;
    List<Weather> weatherList;

    String initial_hour, final_hour;


    public HourChunk(String initial_hour, String final_hour) {
        this.initial_hour = initial_hour;
        this.final_hour = final_hour;
        this.event=new ArrayList<FbEvent>();
        this.hotspots=new ArrayList<HotSpot>();
        this.weatherList = new ArrayList<Weather>();

    }


    public List<Weather> getWeatherList() { return this.weatherList; }

    public void setWeatherList(List<Weather> weatherList) { this.weatherList = weatherList; }

    public List<FbEvent> getEvent() {
        return event;
    }

    public void setEvent(List<FbEvent> event) {
        this.event = event;
    }

    public List<HotSpot> getHotspots() {
        return hotspots;
    }

    public void setHotspots(List<HotSpot> hotspots) {
        this.hotspots = hotspots;
    }

    public String getInitial_hour() {
        return initial_hour;
    }

    public void setInitial_hour(String initial_hour) {
        this.initial_hour = initial_hour;
    }

    public String getFinal_hour() {
        return final_hour;
    }

    public void setFinal_hour(String final_hour) {
        this.final_hour = final_hour;
    }


    public void addWeather (Weather weather) {
        this.weatherList.add(weather);
    }

    public void addEvent (FbEvent event) {
        this.event.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HourChunk hourChunk = (HourChunk) o;

        if (!final_hour.equals(hourChunk.final_hour)) return false;
        if (!initial_hour.equals(hourChunk.initial_hour)) return false;

        return true;
    }


    @Override
    public int hashCode() {
        int result = initial_hour.hashCode();
        result = 31 * result + final_hour.hashCode();
        return result;
    }

    public JSONObject jsonhour( Boolean isTSet ) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("initial_hour",initial_hour);
        obj.put("final_hour",final_hour);

        JSONArray weather = new JSONArray();
        for ( Weather weatherData : weatherList ) {
            weather.put(weatherData.toJson());
        }
        obj.put("TotalWeatherRecord", getWeatherList().size());
        obj.put("Weather", weather);
        float mean = this.getMeanTemperature();
        if ( ! Float.isNaN(mean) ) {
            String meanText = String.format(Locale.US, "%.2f", mean);
            obj.put("MeanTemp", meanText);
        }

        JSONArray events = new JSONArray();
        for (FbEvent single_event : event) {
            events.put(single_event.toJson());
        }
        obj.put("Event", events);

        JSONArray listhotspot = new JSONArray();
        Iterator<HotSpot> iter=hotspots.iterator();
        while (iter.hasNext()) {
            listhotspot.put(iter.next().jsonhotspot(isTSet));
        }
        obj.put("HotSpot",listhotspot);

        return obj;
    }

    public float getMeanTemperature () {
        float meanTemperature = 0f;

        for ( Weather weather : this.getWeatherList() ) {
            meanTemperature += weather.getTemperature();
        }

        if(this.getWeatherList().size()!=0)
            return meanTemperature / this.getWeatherList().size();
        else
            return -100;
    }

    public String csvhour(Boolean isHoliday, Boolean flag) throws JSONException {
        String csvstring="";

        //latitude, longitude, hour, type_day, n_of_events, n_people_attending, Weather=yes

        Iterator<HotSpot> iter=hotspots.iterator();
        while (iter.hasNext()) {
            csvstring+=iter.next().csvhotspot(isHoliday,initial_hour,final_hour,getMeanTemperature(),weatherList,flag);
        }
        return csvstring;
    }
}
