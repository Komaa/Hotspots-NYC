package de.tuberlin.iosp.Detector.Mapper;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * Created by Koma on 22/05/15.
 */
public class AverageCalculator implements MapFunction<Tuple2<Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>>, Tuple3<String, Integer, Integer>> {


    @Override
    public Tuple3<String, Integer, Integer> map(Tuple2<Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>> input) throws Exception {

        return new Tuple3<String, Integer, Integer>(input.f0.f0,input.f0.f1,(input.f0.f2/input.f1.f2));
    }
}
