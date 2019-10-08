package BlockIOThreadPool;

import BlockIOMultiThread.EchoServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 在大量短连接的场景中性能会有提升，但在大量长连接的场景中，没有什么优势
// 适用于小到中度规模的客户端的并发数，如果连接数超过100000或更多，那么性能将很不理想
public class ThreadPoolEchoServer {
    public static int DEFAULT_PORT = 7;

    public static void main(String[] args) throws IOException{
        int port;

        try{
            port = Integer.parseInt(args[0]);
        }catch (RuntimeException ex){
            port = DEFAULT_PORT;
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        Socket clientSocket = null;
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                clientSocket = serverSocket.accept();
                threadPool.submit(new Thread(new EchoServerHandler(clientSocket)));
            }
        }catch(IOException e){
            System.out.println("Exception caught when trying to listen on port " +
                                port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
