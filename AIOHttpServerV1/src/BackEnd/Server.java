/*
异步IO
编译运行步骤
服务器端：
javac ./BackEnd/Server.java
java BackEnd.Server
客户端：
javac ./ClientEnd/Client.java
java ClientEnd.Client

the overall perspective:
1. 客户端的主线程将isReadMode设置成false，将“Hello”存储在buffer里，
接着利用write()将message写入channel，传送并展示到server
2. 服务器read/write处理函数，将该消息写回客户端
3. 因为isReadMode是false，client执行else部分，将isReadMode置为true，并调用read()
读出server写在buffer里的Hello
4. client的completed()方法被调用。取出buffer中的bytes，将其作为服务器应答结果打印出来。
5. client
 */
package BackEnd;

import BackEnd.Attachment;
import BackEnd.ConnectionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class Server {
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
            System.out.printf("BackEnd.Server listening at %s%n", channelServer.getLocalAddress());
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
            System.out.println("BackEnd.Server terminating");
        }
    }
}
