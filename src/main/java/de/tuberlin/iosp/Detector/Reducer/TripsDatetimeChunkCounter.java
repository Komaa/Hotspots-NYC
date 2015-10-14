package de.tuberlin.iosp.Detector.Reducer;

import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.shaded.com.google.common.collect.Iterators;
import org.apache.flink.util.Collector;

import java.util.Iterator;

/**
 * Created by Koma on 19/05/15.
 */

public class TripsDatetimeChunkCounter implements GroupReduceFunction<Tuple4<String, Integer, Integer, Integer>, Tuple5<String, Integer, Integer, Integer, Integer>> {


    @Override
    public void reduce(Iterable<Tuple4<String, Integer, Integer, Integer>> tuple4s, Collector<Tuple5<String, Integer, Integer, Integer, Integer>> tuple5Collector) throws Exception {
        //Reading the first element to have the value of Date, Time, Row, Column and count the number of elements in the iterator
        Iterator<Tuple4<String, Integer, Integer, Integer>> iterator = tuple4s.iterator();
        Tuple4<String, Integer, Integer, Integer> tmp;
        tmp = iterator.next();
        tuple5Collector.collect(new Tuple5<String, Integer, Integer, Integer, Integer>(tmp.f0, tmp.f1, tmp.f2, tmp.f3, Iterators.size(iterator)+1));

        }
    }

