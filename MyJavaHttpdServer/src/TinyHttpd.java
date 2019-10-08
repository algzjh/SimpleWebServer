import java.net.*;
import java.io.*;
import java.util.regex.*;
import java.util.concurrent.*;

public class TinyHttpd {
    public static void main( String argv[] ) throws IOException {
        Executor executor = Executors.newFixedThreadPool(3);
        ServerSocket ss = new ServerSocket( Integer.parseInt(argv[0]) );
        while ( true )
            executor.execute( new TinyHttpdConnection( ss.accept() ) );
            // 使用Executor来服务所有的连接，它带有固定的线程池大小，即可容纳3个线程
    }
}

class TinyHttpdConnection implements Runnable {
    Socket client;
    TinyHttpdConnection ( Socket client ) throws SocketException {
        this.client = client;
    }
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream(), "8859_1" ) );
            OutputStream out = client.getOutputStream();
            PrintWriter pout = new PrintWriter(
                    new OutputStreamWriter(out, "8859_1"), true );
            String request = in.readLine();
            // 从InputStream将GET请求读入变量request，使用BufferedReader包装
            System.out.println( "Request: "+request);

            Matcher get = Pattern.compile("GET /?(\\S*).*").matcher( request );
            if ( get.matches() ) {


                request = get.group(1);
                // 当请求的文件名形如一个目录名（即以斜线结尾），或者为空
                // 为其追加一个我们所熟知的默认文件名index.html
                if ( request.endsWith("/") || request.equals("") )
                    request = request + "index.html";
                System.out.println( "Request: "+request);

                try {
                    FileInputStream fis = new FileInputStream ( request );

                    System.out.println("=================");
                    System.out.println("Total file size to read (in bytes): "
                    + fis.available());
                    int content;
                    while((content = fis.read()) != -1){
                        System.out.print((char) content);
                    }
                    System.out.println("=================");
                    byte [] data = new byte [ 64*1024 ];
                    String mydata = "中国";
                    out.write(mydata.getBytes());
//                    for(int read; (read = fis.read()) != -1; )
//                        out.write(read);
//                    for(int read; (read = fis.read( data )) > -1; )
//                        out.write( data, 0, read );
                    out.flush();
                } catch ( FileNotFoundException e ) {
                    pout.println( "404 Object Not Found" );
                }
            } else
                pout.println( "400 Bad Request" );
            client.close();
        } catch ( IOException e ) {
            System.out.println( "I/O error " + e ); }
    }
}