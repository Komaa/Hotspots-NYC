package de.tuberlin.iosp.Detector.Mapper;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * Created by Koma on 09/07/15.
 */
public class TripsLoaderDrop extends RichFlatMapFunction<String, Tuple4<String, Integer, Integer, Integer>> {


    int citychunknumberLat, citychunknumberLong, hourchunknumber;
    Double eastLimit, westLimit, southLimit, nordLimit, latitudeRange, longitudeRange;


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        //Load variables in every node
        citychunknumberLat=parameters.getInteger("CityChunkLat",100);
        citychunknumberLong=parameters.getInteger("CityChunkLong",100);
        hourchunknumber=parameters.getInteger("HourChunk",4);
        hourchunknumber--;
        eastLimit=parameters.getDouble("eastLimit",-73.699959);
        westLimit=parameters.getDouble("westLimit",-74.259232);
        nordLimit=parameters.getDouble("nordLimit",40.908221);
        southLimit=parameters.getDouble("southLimit",40.490984);
        latitudeRange=nordLimit-southLimit;
        longitudeRange= westLimit-eastLimit;
    }

    @Override
    public void flatMap(String s, Collector<Tuple4<String, Integer, Integer, Integer>> out) throws Exception {

        String pickupDate;
        int dropTime, n_row, n_column;
        Double longitudepoint, latitudepoint;

        String[] lineSplit = s.split(",");
        //Skip the first line of the csv file
        if((!lineSplit[0].equals("medallion"))&&(lineSplit.length>13)) {
          
            longitudepoint = Double.parseDouble(lineSplit[12]);
            latitudepoint = Double.parseDouble(lineSplit[13]);

            //Taking into consideration only starting point inside NYC
            if ((latitudepoint >= southLimit) && (latitudepoint <= nordLimit) && (longitudepoint <= eastLimit) && (longitudepoint >= westLimit)) {
                //Calculating the Row number
                n_row = (int) Math.round((((100 * (longitudepoint - eastLimit) / longitudeRange) * (double) citychunknumberLong) / 100));

                //Calculating the Column number
                n_column = (int) Math.round((((100 * (latitudepoint - southLimit) / latitudeRange) * (double) citychunknumberLat) / 100));

                String[] dateSplit = lineSplit[6].split(" ");
                pickupDate = dateSplit[0];

                // Calculating the hour range (Discrete values)
                String[] hourSplit = dateSplit[1].split(":");
                if(((Integer.parseInt(hourSplit[1]) > 30)||((Integer.parseInt(hourSplit[1]) == 30)&&(Integer.parseInt(hourSplit[2]) > 30)))&&(Integer.parseInt(hourSplit[0])!=23)){
                    dropTime=Integer.parseInt(hourSplit[0])+1;
                }else
                    dropTime=Integer.parseInt(hourSplit[0]);
                dropTime= (int) Math.round((((100 * (dropTime) / 23) * (double) hourchunknumber) / 100));

                out.collect(new Tuple4<String, Integer, Integer, Integer>(pickupDate, dropTime, n_row, n_column));

            }

        }
    }
}




