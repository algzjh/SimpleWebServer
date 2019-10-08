package JavaIOBook;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

import JavaIOBook.Attachment;
import JavaIOBook.ConnectionHandler;
import JavaIOBook.ReadWriteHandler;

public class JavaIOBookServer {
    private final static int PORT = 9090;
    private final static String HOST = "localhost";
    public static void main(String[] args){
        AsynchronousServerSocketChannel channelServer;
        try{
            channelServer = AsynchronousServerSocketChannel.open();
            // return a new asynchronous server socket channel
            channelServer.bind(new InetSocketAddress(HOST, PORT));
            // bind the channel socket to a local address
            // and configures the socket to listen for connections
            System.out.printf("Server listening at %s%n", channelServer.getLocalAddress());
        }catch (IOException ioe){
            System.err.println("Unable to open or bind server socket channel");
            return;
        }
        Attachment att = new Attachment();
        att.channelServer = channelServer;
        channelServer.accept(att, new ConnectionHandler());
        // The completion handler for an IO operation initiated on a channel bound to
        // a group is guaranteed to be invoked by one of the pooled threads in the group.
        
        try{
            Thread.currentThread().join();
            // wait for the currently executing thread to die
        }catch(InterruptedException ie){
            System.out.println("Server terminating");
        }
    }
}
