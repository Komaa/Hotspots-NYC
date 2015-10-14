package de.tuberlin.iosp.Detector.Mapper;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.util.Collector;

/**
 * Created by Koma on 09/06/15.
 */
public class HotspotSelector implements FlatMapFunction<Tuple7<String, String, String, Double, Double, Integer, Boolean>, Tuple6<String, String, String, Double, Double, Integer>> {


    @Override
    public void flatMap(Tuple7<String, String, String, Double, Double, Integer, Boolean> input, Collector<Tuple6<String, String, String, Double, Double, Integer>> tuple6Collector) throws Exception {
        if(input.f6)
        tuple6Collector.collect(new Tuple6<String, String, String, Double, Double, Integer>(input.f0,input.f1,input.f2,input.f3,input.f4,input.f5));
    }
}
