package streams.cta.io;

import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

/**
 * Created by alexey on 13/08/15.
 */
public class TestWordSpout extends BaseRichSpout {

    SpoutOutputCollector _collector;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("word"));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        _collector = spoutOutputCollector;
    }

    @Override
    public void nextTuple() {
        Utils.sleep(100);
        //final String[] words = new String[]{"nathan", "mike", "jackson", "golda", "bertels"};
        //final Random rand = new Random();
        //final String word = words[rand.nextInt(words.length)];
        final byte[] word = new byte[2048];
        _collector.emit(new Values(word));
    }

    @Override
    public void ack(Object o) {

    }

    @Override
    public void fail(Object o) {

    }
}
