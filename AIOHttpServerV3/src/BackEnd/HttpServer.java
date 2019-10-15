/*
经典的Java IO方法，利用TestConcurrence可以测试其并发性
http://127.0.0.1:2333/Hello

编译步骤：
javac ./BackEnd/HttpServer.java
java BackEnd.HttpServer
 */
package BackEnd;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import BackEnd.HandleRequest;

public class HttpServer extends Thread {

    private final int HTTP_PORT;

    private ServerSocket listen = null;

    private boolean running = true;

    private Hashtable knownRequests = new Hashtable();

    public HttpServer(int port) {
        this.HTTP_PORT = port;

        if (this.HTTP_PORT <= 0) {
            System.err.println("HttpServer not started, as -Port is " +
                    this.HTTP_PORT);
            return;
        }

        System.err.println("Creating new HttpServer on Port = " +
                this.HTTP_PORT);

        setDaemon(true);

        start();
    }

    public void registerRequest(String urlPath, String data) {
        System.out.println("Registering urlPath: " +
                urlPath + "=" + data);
        knownRequests.put(urlPath.trim(), data);
    }


    public void run() {
        try {

            this.listen = new ServerSocket(HTTP_PORT);

            while (running) {

                Socket accept = this.listen.accept();
                System.out.println("New incoming request on Port=" +
                        HTTP_PORT + " ...");
                if (!running) {
                    System.out.println("Closing http server Port=" +
                            HTTP_PORT + ".");
                    break;
                }
			/*创建一个服务线程，当HandlerRequest向客户端发回响应后，所创建的线程也将结束*/
                HandleRequest hh = new HandleRequest
                        (accept, knownRequests);
            }

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

        if (this.listen != null) {
            try {

                this.listen.close();
            } catch (java.io.IOException e) {
                System.out.println("this.listen.close()" + e.toString());
            }
            this.listen = null;
        }
    }

    //
    public static void main(String[] args) {

        HttpServer server = new HttpServer(2333);
		/*当客户端请求的url是”/Hello”时，返回”Hello World”*/
        server.registerRequest("/Hello",
                "Hello World from  MiniHttpServer!");
        server.registerRequest("Counter",
                "");
        System.out.println("Press any key to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

