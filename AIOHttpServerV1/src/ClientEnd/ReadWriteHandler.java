package ClientEnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

public class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {
    private final static Charset CSUTF8 = Charset.forName("UTF-8");

    private BufferedReader conReader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void completed(Integer result, Attachment att){
        /*
        complete method is called to respond to a successful read() or write(),
        which includes issuing a counterpart write() or read(). Its failed() method
        is called when the client breaks off its connection with the server.
         */
        if(att.isReadMode){
            att.buffer.flip();
            int limit = att.buffer.limit();
            byte[] bytes = new byte[limit];
            att.buffer.get(bytes, 0, limit);
            String msg = new String(bytes, CSUTF8);
            System.out.printf("Server responded: %s%n", msg);
            /*
            if a read() has been performed and respond accordingly.
            If so, it extracts the buffer's bytes into an array, which
            is converted to a string that is subsequently output.
             */
            // let user to enter a message that will be sent to the server
            try{
                msg = "";
                while(msg.length() == 0){
                    System.out.print("Enter message (\" end\" to quit): ");
                    msg = conReader.readLine();
                }
                if(msg.equalsIgnoreCase("end")){
                    // the client thread is interrupted and the client terminates
                    att.mainThd.interrupt();
                    return;
                }
            }catch (IOException ioe){
                System.err.println("Unable to read from console");
            }

            /*
            other message's bytes are extracted from the string and stored in the buffer,
            which is subsequently written to the socket channel.
             */
            att.isReadMode = false;
            att.buffer.clear();
            byte[] data = msg.getBytes(CSUTF8);
            att.buffer.put(data);
            att.buffer.flip();
            att.channel.write(att.buffer, att, this);
        }else{
            att.isReadMode = true;
            att.buffer.clear();
            att.channel.read(att.buffer, att, this);

        }
    }

    @Override
    public void failed(Throwable t, Attachment att){
        System.err.println("Server not responding");
        System.exit(1);
    }
}

/*
the overall perspective:
1. 客户端的主线程将isReadMode设置成false，将“Hello”存储在buffer里，
接着利用write()将message写入channel，传送并展示到server
2. 服务器read/write处理函数，将该消息写回客户端
3. 因为isReadMode是false，client执行else部分，将isReadMode置为true，并调用read()
读出server写在buffer里的Hello
4. client的completed()方法被调用。取出buffer中的bytes，将其作为服务器应答结果打印出来。
5. client
*/
