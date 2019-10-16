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
package ClientEnd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import ClientEnd.Attachment;
import ClientEnd.ReadWriteHandler;

public class Client {
    private final static Charset CSUTF8 = Charset.forName("UTF-8");
    private final static int PORT = 9090;
    private final static String HOST = "localhost";

    public static void main(String[] args){
        AsynchronousSocketChannel channel;
        try{
            channel = AsynchronousSocketChannel.open();
        }catch (IOException ioe){
            System.err.println("Unable to open client socket channel");
            return;
        }
        try{
            channel.connect(new InetSocketAddress(HOST, PORT)).get();
            System.out.printf("Client at %s connected%n",
                    channel.getLocalAddress());
        }catch (ExecutionException | InterruptedException eie){
            System.err.println("Server not responding");
            return;
        }catch (IOException ioe){
            System.err.println("Unable to obtain client socket channel's local address");
            return;
        }

        Attachment att = new Attachment();
        att.channel = channel;
        att.isReadMode = false;
        att.buffer = ByteBuffer.allocate(2048);
        // An initial message is created and stored in a buffer,
        // which is then written to the socket channel
        att.mainThd = Thread.currentThread();

        byte[] data = "Hello".getBytes(CSUTF8);
        att.buffer.put(data);
        att.buffer.flip();
        channel.write(att.buffer, att, new ReadWriteHandler());

        try{
            att.mainThd.join();
            // main()'s thread of execution blocks itself by calling Thread.join()
            // The only way to unblock this thread and reture from main() is to
            // interrupt the thread from another thread.
        }catch (InterruptedException ie){
            System.out.println("Client terminating");
        }
    }
}

