/*
非阻塞IO
需要使用firefox，因为只实现了HTTP0.9
只有GET方法
http://127.0.0.1:2333/welcome.html
编译运行方法：
javac ./BackEnd/Server.java
java BackEnd.Server
 */
package BackEnd;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.regex.*;

public class Server // 主类：接受连接
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
        new Server().run( 2333, 3/*threads*/ );
    }
}
