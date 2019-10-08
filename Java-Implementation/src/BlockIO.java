import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


// 服务器启动后，等待客户端连接，在客户端连接服务器后，服务器就阻塞读写数据流
public class BlockIO {
    public static int DEFAULT_PORT = 7;

    public static void main(String[] args) throws IOException{
        int port;
        try{
            port = Integer.parseInt(args[0]);
        }catch (RuntimeException ex){
            port = DEFAULT_PORT;
        }

        try(ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
            String inputLine;
            while((inputLine = in.readLine()) != null){
                out.println(inputLine);
            }
        }catch (IOException e){
            System.out.println("Exception caught when trying to listen on port " +
                                port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
