package de.tuberlin.iosp.Detector.Mapper;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * Created by Koma on 22/05/15.
 */
public class CalculateHotspots extends RichFlatMapFunction<Tuple2<Tuple5<String, Integer, Integer, Integer, Integer>, Tuple3<String, Integer, Integer>>, Tuple7<String, String, String, Double, Double, Integer, Boolean>> {

    int citychunknumberLat, citychunknumberLong, hourchunknumber, hourchunk, max_hourchunk;
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

        hourchunk=24/(hourchunknumber+1);
        max_hourchunk=hourchunk*hourchunknumber;
    }

    @Override
    public void flatMap(Tuple2<Tuple5<String, Integer, Integer, Integer, Integer>, Tuple3<String, Integer, Integer>> input, Collector<Tuple7<String, String, String, Double, Double, Integer, Boolean>> collector) throws Exception {

        int pickupTime;
        Double longitudepoint, latitudepoint;
        Boolean ishotspot=false;
        //Set a limit to not consider day and time with few data

            if(input.f0.f4>=input.f1.f2)
                ishotspot=true;


                //Calculating the longitude, latitude and chunk of time
                longitudepoint=(((double)((100*input.f0.f2)/citychunknumberLong)*longitudeRange)/100)+eastLimit;
                latitudepoint=(((double)((100*input.f0.f3)/citychunknumberLat)*latitudeRange)/100)+southLimit;
                pickupTime= (int) Math.round((((100 * (input.f0.f1) / hourchunknumber) * max_hourchunk) / 100));

                collector.collect(new Tuple7<String, String, String, Double, Double, Integer, Boolean>(input.f0.f0,pickupTime+":00", (pickupTime+hourchunk)+":00",longitudepoint,latitudepoint,input.f0.f4,ishotspot));

    }
}
