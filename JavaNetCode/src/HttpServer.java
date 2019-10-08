import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class HttpServer extends Thread {
    //继承自Thread，类HttpServer实现的Run()方法作为Http服务器的主控线程
    // HTTP服务器的端口
    private final int HTTP_PORT;
    // ServerSocket, 服务器通过这个ServerSocket接收来自Web客户的连接请求
    private ServerSocket listen = null;
    //布尔变量，指示是否要继续运行
    private boolean running = true;
    //客户的请求和对相应请求的应答列表
    private Hashtable knownRequests = new Hashtable();
    //构造函数，有一个参数port，指示Http服务器所在的端口
    public HttpServer(int port) {
        this.HTTP_PORT = port;
        //错误处理，端口号应该是大于零的整数
        if (this.HTTP_PORT <= 0) {
            System.err.println("HttpServer not started, as -Port is " +
                    this.HTTP_PORT);
            return;
        }
		/*此处将信息输出的System.err中，处于醒目的目的，在某些控制台上可以看到
             红色的输出*/
        System.err.println("Creating new HttpServer on Port = " +
                this.HTTP_PORT);
        //设置主线程为精灵线程
        setDaemon(true);
        //开始主控线程运行，此后可以接收来自客户的请求并处理
        start();
    }
    /*注册urlPath和data对，服务器从knowRequest表中匹配客户端的请求， 并
               将匹配的内容返回客户端*/
    public void registerRequest(String urlPath, String data) {
        System.out.println("Registering urlPath: " +
                urlPath + "=" + data);
        knownRequests.put(urlPath.trim(), data);
    }
    public void removeRequest(String urlPath) {
        knownRequests.remove(urlPath.trim());
    }
    //实现父类Thread的run方法
    public void run() {
        try {
            //创建ServerSocket的实例，ServerSocket绑定到HTTP_PORT端口
            this.listen = new ServerSocket(HTTP_PORT);
            //如果running标志为true，那么继续运行，否则主服务线程退出，服务结束
            while (running) {
                //接收来自客户端的TCP连接
                Socket accept = this.listen.accept();
                System.out.println("New incoming request on Port=" +
                        HTTP_PORT + " ...");
                if (!running) {
                    System.out.println("Closing http server Port=" +
                            HTTP_PORT + ".");
                    break;
                }
			/*创建一个服务线程，为这个连接服务。为了同时为多个Web客户服务，不能在这
              里就为客户服务，创建类HandlerRequest的实例，HandlerRequest也是
            Thread类的子类，HandlerRequset为accept所代表的客户端与本地的TCP
              连接服务，当HandlerRequest向客户端发回响应后，所创建的线程也将结束*/
                HandleRequest hh = new HandleRequest
                        (accept, knownRequests);
            }
            // 处理在服务过程中发生的异常
        } catch (java.net.BindException e) {
            System.out.println(	"HTTP server problem, Port "	+
                    listen.getInetAddress().toString()	+
                    ":" + HTTP_PORT+" is not available: "	+
                    e.toString());
        } catch (java.net.SocketException e) {
            System.out.println(	"Socket "	+ listen.getInetAddress()
                    .toString()+ ":" + HTTP_PORT+ " closed successfully: "
                    + e.toString());
        } catch (IOException e) {
            System.out.println("HTTP server problem on port : " +
                    HTTP_PORT + ": " + e.toString());
        }
        // 主循环结束，线程将要退出，做清扫工作
        if (this.listen != null) {
            try {
                //关闭套接字
                this.listen.close();
            } catch (java.io.IOException e) {
                System.out.println("this.listen.close()" + e.toString());
            }
            this.listen = null;
        }
    }
    // 可以调用这个方法停止HttpServer的服务
    public void shutdown()
    {
        System.out.println("Entering shutdown");
        //设置标志，通知主服务线程不再为新连接服务
        running = false;
        try {
            // 关闭套接字
            if (this.listen != null) {
                this.listen.close();
                this.listen = null;
            }
        } catch (java.io.IOException e) {
            System.out.println("shutdown problem: " + e.toString());
        }
    }
    //主程序的入口
    public static void main(String[] args) {
        //创建一个HttpServer的实例，使用8888端口
        HttpServer server = new HttpServer(8888);
		/*当客户端请求的url是”/Hello”时，返回”Hello World
             from MiniHttpServer!”*/
        server.registerRequest("/Hello",
                "Hello World from  MiniHttpServer!");
        // 按任意建退出程序
        System.out.println("Press any key to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
