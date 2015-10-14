package de.tuberlin.iosp.Detector.Reducer;

import com.google.common.collect.Iterators;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.util.Collector;

import java.util.Iterator;

/**
 * Created by Koma on 22/05/15.
 */
public class ChunkwithpeopleCounter implements GroupReduceFunction<Tuple5<String, Integer, Integer, Integer, Integer>, Tuple3<String, Integer, Integer>> {
    @Override
    public void reduce(Iterable<Tuple5<String, Integer, Integer, Integer, Integer>> tuple5s, Collector<Tuple3<String, Integer, Integer>> tuple3Collector) throws Exception {
        //Reading the first element to have the value of Date and Time and count the number of elements in the iterator
        Iterator<Tuple5<String, Integer, Integer, Integer, Integer>> iterator = tuple5s.iterator();
        Tuple5<String, Integer, Integer, Integer, Integer> tmp;
        tmp = iterator.next();
        tuple3Collector.collect(new Tuple3<String, Integer, Integer>(tmp.f0, tmp.f1, Iterators.size(iterator)+1));
    }
}
