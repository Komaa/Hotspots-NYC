package de.tuberlin.iosp.Detector;


import de.tuberlin.iosp.Detector.Mapper.*;
import de.tuberlin.iosp.Detector.Reducer.ChunkwithpeopleCounter;
import de.tuberlin.iosp.Detector.Reducer.TripsDatetimeChunkCounter;
import de.tuberlin.iosp.Detector.Reducer.TripsDatetimeCounter;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;

public class HotspotDetector {

    public void run() {

        ExecutionEnvironment environment = ExecutionEnvironment.getExecutionEnvironment();

        //Set configuration class in order to pass variables useful for every node
        Configuration config= new Configuration();
        config.setInteger("CityChunkLat", Config.citychunklat());
        config.setInteger("CityChunkLong", Config.citychunklong());
        config.setInteger("HourChunk", Config.hourchunk());
        config.setDouble("eastLimit", Config.eastLimit());
        config.setDouble("westLimit", Config.westLimit());
        config.setDouble("southLimit", Config.southLimit());
        config.setDouble("nordLimit", Config.nordLimit());

        //Load input file
        DataSource<String> inputTrips = environment.readTextFile(Config.pathToInput());

        // Convert the input to trips, consisting of (Data, Time, Row, Column)
        DataSet<Tuple4<String, Integer, Integer, Integer>> pickpoint = inputTrips.flatMap(new TripsLoader())
                .withParameters(config);

        DataSet<Tuple4<String, Integer, Integer, Integer>> droppoint = inputTrips.flatMap(new TripsLoaderDrop())
                .withParameters(config);

        //Count the number of trips in a specific day and time and return a Tuple3 consisting of (Data, Time, Number)
        DataSet<Tuple3<String, Integer, Integer>> numbertrips_datetime=pickpoint.groupBy(0,1)
                .reduceGroup(new TripsDatetimeCounter());

        DataSet<Tuple3<String, Integer, Integer>> numbertrips_datetime_drop=droppoint.groupBy(0,1)
                .reduceGroup(new TripsDatetimeCounter());

        //Count the number of trips in a specific day, time and chunk and return a Tuple5 consisting of (Data, Time, Row, Column, Number)
        DataSet<Tuple5<String, Integer, Integer, Integer, Integer>> numbertrips_datetime_chunk=pickpoint.groupBy(0,1,2,3)
                .reduceGroup(new TripsDatetimeChunkCounter());

        DataSet<Tuple5<String, Integer, Integer, Integer, Integer>> numbertrips_datetime_chunk_drop=droppoint.groupBy(0,1,2,3)
                .reduceGroup(new TripsDatetimeChunkCounter());

        //Count the number of chunk in a specific day and time with at least one starting point and return a Tuple3 consisting of (Data, Time, Number)
        // [Necessary since the shape of NYC is not rectangular, to avoid in the average chunks that are in the sea]
        DataSet<Tuple3<String, Integer, Integer>> numberChunkswithpeople=numbertrips_datetime_chunk
                .groupBy(0,1).reduceGroup(new ChunkwithpeopleCounter());

        DataSet<Tuple3<String, Integer, Integer>> numberChunkswithpeople_drop=numbertrips_datetime_chunk_drop
                .groupBy(0,1).reduceGroup(new ChunkwithpeopleCounter());

        //This method perform the join between the Dataset contains the number of trips in a specific day and time
        //and the number of chunk in a specific day and time with at least one starting point for computing the average
        DataSet<Tuple3<String, Integer, Integer>> averageNumbertrips_datetime=numbertrips_datetime
                .join(numberChunkswithpeople).where(0,1).equalTo(0,1).map(new AverageCalculator());

        DataSet<Tuple3<String, Integer, Integer>> averageNumbertrips_datetime_drop=numbertrips_datetime_drop
                .join(numberChunkswithpeople_drop).where(0,1).equalTo(0,1).map(new AverageCalculator());

        //Calculate the hotspot in a specific day and time and return return a Tuple5 consisting of  (Data, Time, Row, Column, Number)
        DataSet<Tuple7<String, String, String, Double, Double, Integer, Boolean>> arehotspots=
             numbertrips_datetime_chunk.join(averageNumbertrips_datetime).where(0,1).equalTo(0,1).
                     flatMap(new CalculateHotspots()).withParameters(config);

        DataSet<Tuple7<String, String, String, Double, Double, Integer, Boolean>> arehotspots_drop=
                numbertrips_datetime_chunk_drop.join(averageNumbertrips_datetime_drop).where(0,1).equalTo(0,1).
                        flatMap(new CalculateHotspots()).withParameters(config);

        DataSet<Tuple6<String, String, String, Double, Double, Integer>>hotspots=arehotspots.flatMap(new HotspotSelector());

        DataSet<Tuple6<String, String, String, Double, Double, Integer>>hotspots_drop=arehotspots_drop.flatMap(new HotspotSelector());

        //Print the size (latitude, longitude) of every chunk
        System.out.println("Size in longitude of a chunk:" + Math.abs(Config.westLimit()-Config.eastLimit())/Config.citychunklong());
        System.out.println("Size in latitude of a chunk:" + ((Config.nordLimit() - Config.southLimit()) / Config.citychunklat()) + "\n");

        //Write the output as csv file
        hotspots.writeAsCsv(Config.outputPath() + "Hotspots/Pickup/", FileSystem.WriteMode.OVERWRITE);
        hotspots_drop.writeAsCsv(Config.outputPath() + "Hotspots/Drop/", FileSystem.WriteMode.OVERWRITE);
        System.out.println("Raw Hotspot Data written to: " + Config.outputPath() + "Hotspots/");

        arehotspots.writeAsCsv(Config.outputPath() + "DatasetPrediction/Pickup/", FileSystem.WriteMode.OVERWRITE);
        arehotspots_drop.writeAsCsv(Config.outputPath() + "DatasetPrediction/Drop/", FileSystem.WriteMode.OVERWRITE);
        System.out.println("Raw DatasetPrediction Data written to: " + Config.outputPath() + "DatasetPrediction/");


        try {
            environment.execute("HotspotDetector");
        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

    }


}
