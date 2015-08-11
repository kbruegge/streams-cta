package streams.cta.io;

//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import org.zeromq.ZMQ;

public class TestZeroMQ {

    public static void main(String[] args) throws Exception {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to clients
        ZMQ.Socket responder = context.socket(ZMQ.REP);
        responder.bind("tcp://*:5555");

        while (!Thread.currentThread().isInterrupted()) {
            // Wait for next request from the client
            byte[] request = responder.recv(0);
            System.out.println("Received Hello");

            // Do some 'work'
            Thread.sleep(1000);

            // Send reply back to client
            String reply = "World";
            responder.send(reply.getBytes(), 0);
        }
        responder.close();
        context.term();
    }
}
