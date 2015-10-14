package de.tuberlin.iosp.Parser;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Koma on 02/06/15.
 */
public class HotSpot {
    double latitude, longitude;
    int n_people;
    boolean ishotspot;
    int n_fb_people;
    int n_fb_events;

    public int getN_fb_people() {
        return n_fb_people;
    }

    public int getN_fb_events() {
        return n_fb_events;
    }

    public void incrementN_fb_events() {
        this.n_fb_events = n_fb_events + 1;
    }

    public void incN_fb_people(int n_fb_people) {
        this.n_fb_people += n_fb_people;
    }

    public HotSpot(double latitude, double longitude, int n_people) {
        this.n_fb_events = 0;
        this.n_fb_people = 0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.n_people = n_people;
    }

    public boolean isIshotspot() {
        return ishotspot;
    }

    public void setIshotspot(boolean ishotspot) {
        this.ishotspot = ishotspot;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getN_people() {
        return n_people;
    }

    public void setN_people(int n_people) {
        this.n_people = n_people;
    }

    public JSONObject jsonhotspot( Boolean isTSet ) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("latitude",latitude);
        obj.put("longitude",longitude);
        obj.put("people",n_people);
        obj.put("fb_people",n_fb_people);
        obj.put("fb_events",n_fb_events);

        if ( isTSet ) {
            obj.put("isHotspot", ishotspot);
        }

        return obj;
    }

    public String csvhotspot(Boolean isholiday, String initial_hour, String final_hour, Float meantemp, List<Weather> weather, Boolean flag) throws JSONException {
        String tmpWeather = "?";

        if (weather.size() > 0) {

            int max = 0;
            Map<String, Integer> freq = new HashMap<String, Integer>();
            Iterator<Weather> iter = weather.iterator();
            while (iter.hasNext()) {
                Weather tmp = iter.next();
                Integer count = freq.get(tmp.getConditions());
                if (count == null) {
                    freq.put(tmp.getConditions(), 1);
                } else {
                    freq.put(tmp.getConditions(), count + 1);
                }
            }

            for (String key : freq.keySet()) {
                //System.out.println(key + " :: " + freq.get(key));
                if (freq.get(key) > max) {
                    max = freq.get(key);
                    tmpWeather = key;
                }

            }
        }
        String csvstring;
        if(meantemp==-100){
            if(flag)
                csvstring=""+ latitude + "," + longitude+ "," + initial_hour+ "," +isholiday + ",?," + tmpWeather.replaceAll("\\s+","") + "," + n_fb_events + "," + n_fb_people + "," + n_people + "\n";
            else
                csvstring=""+ latitude + "," + longitude+ "," + initial_hour+ "," +isholiday + ",?," + tmpWeather.replaceAll("\\s+","") + "," + n_fb_events + "," + n_fb_people + "," + ishotspot + "\n";

        }else
            if(flag)
                csvstring=""+ latitude + "," + longitude+ "," + initial_hour+ "," +isholiday + "," + meantemp + "," + tmpWeather.replaceAll("\\s+","") + "," + n_fb_events + "," + n_fb_people + "," + n_people + "\n";
            else
                csvstring=""+ latitude + "," + longitude+ "," + initial_hour+ "," +isholiday + "," + meantemp + "," + tmpWeather.replaceAll("\\s+","") + "," + n_fb_events + "," + n_fb_people + "," + ishotspot + "\n";
        return csvstring;
    }

}
