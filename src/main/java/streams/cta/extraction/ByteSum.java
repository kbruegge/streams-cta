package streams.cta.extraction;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import streams.cta.CTARawDataProcessor;
import streams.cta.CTATelescope;

import java.time.LocalDateTime;

/**
 * Just a test processor  that sums ob bytes from an array. this will be used as long as we have no protobufs to serialize events from zmq
 * Created by kai on 11.08.15.
 */
public class ByteSum implements Processor {

     @Override
    public Data process(Data input) {
         byte[] data = (byte[]) input.get("data_bytes");
         long sum = 0;
         if(data != null){

             for (byte b : data){
                 sum += b;
             }
             input.put("data_sum", sum);
         }
         return input;
    }
}
