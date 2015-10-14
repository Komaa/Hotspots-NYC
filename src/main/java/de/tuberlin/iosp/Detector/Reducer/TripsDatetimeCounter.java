package de.tuberlin.iosp.Detector.Reducer;

import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.shaded.com.google.common.collect.Iterators;
import org.apache.flink.util.Collector;

import java.util.Iterator;

/**
 * Created by Koma on 19/05/15.
 */
public class TripsDatetimeCounter implements GroupReduceFunction<Tuple4<String, Integer, Integer, Integer>, Tuple3<String, Integer, Integer>> {


    @Override
    public void reduce(Iterable<Tuple4<String, Integer, Integer, Integer>> tuple4s, Collector<Tuple3<String, Integer, Integer>> tuple3Collector) throws Exception {
        //Reading the first element to have the value of Date and Time and count the number of elements in the iterator
        Iterator<Tuple4<String, Integer, Integer, Integer>> iterator = tuple4s.iterator();
        Tuple4<String, Integer, Integer, Integer> tmp;
            tmp = iterator.next();
        tuple3Collector.collect(new Tuple3<String, Integer, Integer>(tmp.f0, tmp.f1, Iterators.size(iterator)+1));
    }
}
