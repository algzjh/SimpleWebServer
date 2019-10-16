/*
需要使用firefox，因为只实现了http0.9
http://127.0.0.1:1235/welcome.html
编译运行方法
javac LargerHttpd.java
java LargerHttpd
 */
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.regex.*;

public class LargerHttpd // 主类：接受连接
{
    /*
    接受新的客户端套接字连接，将其包装在HttpdConnection的一个实例
    然后使用一个Selector来查看客户端的状态
    一旦检测出客户端已经准备好发送或接受数据，则将一个Runnable任务转交给Executor
    该任务对相应客户端调用read()或write()
    */
    Selector clientSelector;

    public void run( int port, int threads ) throws IOException
    {
        clientSelector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 配置为非阻塞模式
        InetSocketAddress sa =  new InetSocketAddress( InetAddress.getLoopbackAddress(), port );
        ssc.socket().bind( sa );
        ssc.register( clientSelector, SelectionKey.OP_ACCEPT ); // 向主Selector进行注册

        Executor executor = Executors.newFixedThreadPool( threads );

        while ( true ) {
            try {
                while ( clientSelector.select(100) == 0 );
                Set<SelectionKey> readySet = clientSelector.selectedKeys();
                for(Iterator<SelectionKey> it=readySet.iterator(); it.hasNext();)
                {
                    final SelectionKey key = it.next();
                    it.remove();
                    if ( key.isAcceptable() ) {
                        acceptClient( ssc ); // 检查键是否准备好进行某个操作
                    } else {
                        key.interestOps( 0 ); // 将兴趣集置零
                        executor.execute( new Runnable() {
                            // 通过Runnable任务将该键分配给handleClient()方法。
                            public void run() {
                                try {
                                    handleClient( key );
                                } catch ( IOException e) { System.out.println(e); }
                            }
                        } );
                    }
                }
            } catch ( IOException e ) { System.out.println(e); }
        }
    }

    void acceptClient( ServerSocketChannel ssc ) throws IOException
    {
        /*
        接受一个新的socket连接，使用选择器配置并注册该链接，
        并带有一个初始的兴趣集合以供读取。
        最后，为该socket创建HttpdConnection，并且把HttpdConnection对象附加给键以供后续访问。
         */
        SocketChannel clientSocket = ssc.accept();
        clientSocket.configureBlocking(false);
        SelectionKey key =  clientSocket.register( clientSelector, SelectionKey.OP_READ );
        HttpdConnection client = new HttpdConnection( clientSocket );
        key.attach( client );
    }

    void handleClient( SelectionKey key ) throws IOException
    {
        HttpdConnection client = (HttpdConnection)key.attachment();
        if ( key.isReadable() ) {
            client.read( key );
        } else {
            client.write( key );
        }
        clientSelector.wakeup();
    }

    public static void main( String argv[] ) throws IOException {
        //new LargerHttpd().run( Integer.parseInt(argv[0]), 3/*threads*/ );
        new LargerHttpd().run( 1235, 3/*threads*/ );
    }
}

class HttpdConnection {  // 连接类：封装套接字，并处理与客户端的会话
    static Charset charset = Charset.forName("8859_1");
    static Pattern httpGetPattern = Pattern.compile("(?s)GET /?(\\S*).*");
    SocketChannel clientSocket;
    ByteBuffer buff = ByteBuffer.allocateDirect( 64*1024 );
    String request;
    String response;
    FileChannel file;
    int filePosition;

    HttpdConnection ( SocketChannel clientSocket ) {
        this.clientSocket = clientSocket;
    }

    void read( SelectionKey key ) throws IOException {
        if ( request == null && (clientSocket.read( buff ) == -1
                || buff.get( buff.position()-1 ) == '\n' ) )
            processRequest( key );
        else
            key.interestOps( SelectionKey.OP_READ );
    }

    void processRequest( SelectionKey key ) {
        buff.flip();
        request = charset.decode( buff ).toString();
        System.out.println("request: " + request);
        Matcher get = httpGetPattern.matcher( request );
        if ( get.matches() ) {
            request = get.group(1);
            if ( request.endsWith("/") || request.equals("") )
                request = request + "index.html";
            System.out.println( "Request: "+request);
            try {
                file = new FileInputStream ( request ).getChannel();
            } catch ( FileNotFoundException e ) {
                response = "404 Object Not Found";
            }
        } else
            response = "400 Bad Request" ;

        if ( response != null ) {
            buff.clear();
            charset.newEncoder().encode(
                    CharBuffer.wrap( response ), buff, true );
            buff.flip();
        }
        key.interestOps( SelectionKey.OP_WRITE );
    }

    void write( SelectionKey key ) throws IOException {
        if ( response != null ) {
            clientSocket.write( buff );
            if ( buff.remaining() == 0 )
                response = null;
        } else if ( file != null ) {
            int remaining = (int)file.size()-filePosition;
            long sent = file.transferTo( filePosition, remaining, clientSocket);
            if ( sent >= remaining || remaining <= 0 ) {
                file.close();
                file = null;
            } else
                filePosition += sent;
        }
        if ( response == null && file == null ) {
            clientSocket.close();
            key.cancel();
        } else
            key.interestOps( SelectionKey.OP_WRITE );
    }
}