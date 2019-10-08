import java.net.*;
import java.util.*;
import java.io.*;
class HandleRequest extends Thread {
    // socket，服务器accept的套接字
    private final Socket sock;
    private final Hashtable knownRequests;
    private final String CRLF = "\r\n";
    public HandleRequest(Socket sock, Hashtable knownRequests) {
        this.sock = sock;
        this.knownRequests = knownRequests;
        //开始线程，为来自客户端的请求服务
        start();
    }
    // 继承自Thread类的run()方法
    public void run() {
        System.out.println("Handling client request,...");
        BufferedReader iStream = null;
        DataOutputStream oStream = null;
        String clientRequest = "";
        boolean first = true;
        try {
            //从socket中获取输入、输出流
            iStream = new BufferedReader(new
                    InputStreamReader(sock.getInputStream()));
            oStream = new DataOutputStream(sock.getOutputStream());
            //从客户端读取请求信息
            clientRequest = iStream.readLine();
            String headerLine;
            while (true /*!sock.isClosed() JDK 1.4 only*/) {
                //读取HTTP请求的首部信息
                headerLine = iStream.readLine();
                System.out.println("Receiving header '" +
                        headerLine + "'");
                if (headerLine == null || headerLine.trim()
                        .length() < 1) {
                    break;
                }
            }
            System.out.println("Request from client"+getSocketInfo());
            if (clientRequest == null) {
                String info = "Empty request ignored " +
                        getSocketInfo();
                errorResponse(oStream, "HTTP/1.1 400 Bad Request",
                        null, true, info);
                System.out.println(info);
                return;
            }
            first = false;
            System.out.println("Handling client request '" +
                    clientRequest+ "' ...");
            StringTokenizer toks = new StringTokenizer(clientRequest);
            //一个请求字符串由三部分组成，例如“GET /Hello HTTP/1.0”
            if (toks.countTokens() != 3) {
                String info = "Wrong syntax in client request: '"+
                        clientRequest	+ "', closing "+getSocketInfo()+
                        " connection.";
                errorResponse(oStream, "HTTP/1.1 400 Bad Request",
                        null, true,info);
                System.out.println(info);
                return;
            }
            String method = toks.nextToken(); // "GET"
            String resource = toks.nextToken(); // "/Hello"
            String version = toks.nextToken(); // "HTTP/1.0"
            //作为简易的HTTP服务器，只处理GET和HEAD
            if (!method.equalsIgnoreCase("GET")
                    && !method.equalsIgnoreCase("HEAD")) {
                String info =	"Invalid method in client "	+
                        getSocketInfo()+ " request: '"+ clientRequest+ "'";
                errorResponse(oStream,"HTTP/1.1 501 Method Not Implemented",
                        "Allow: GET",	true, info);
                System.out.println(info);
                return;
            }
            //从knownRequest列表中查找响应字符串
            String responseStr = (String)
                    knownRequests.get(resource.trim());
            //没有找到，生成错误信息
            if (responseStr == null) {
                String info ="Ignoring unknown data '"+ resource.trim()
                        + "' from client "+ getSocketInfo()+
                        " request: '" + clientRequest+ "'";
                errorResponse(oStream, "HTTP/1.1 404 Not Found",
                        null, true,info);
                System.out.println(info);
                return;
            }
            //发会服务成功的状态
            errorResponse(oStream, "HTTP/1.1 200 OK", null, false, null);
            //生成首部域Content-Length
            String length = "Content-Length: " + responseStr.length();
            oStream.write((length + CRLF).getBytes());
            //生成Content-Type首部域
            oStream.write(("Content-Type: text/plain; charset=iso-8859-1"
                    + CRLF).getBytes());
            if (!method.equalsIgnoreCase("HEAD")) {
                oStream.write(CRLF.getBytes());
                oStream.write(responseStr.getBytes());
            }
            oStream.flush();
            //处理异常
        } catch (IOException e) {
            if (clientRequest == null && first) {
                System.out.println("Ignoring connect/disconnect attempt");
            } else {
                System.out.println(	"Problems with sending response for '"
                        + clientRequest	+ "' to client "+ getSocketInfo()
                        + ": " + e.toString());
            }
        }
        finally {
            try {
                if (iStream != null)
                    iStream.close();
            } catch (IOException e) {}
            try {
                if (oStream != null)
                    oStream.close();
            } catch (IOException e) {}
            try {
                sock.close();
            } catch (IOException e) {}
        }
    }

    /*调用此方法向oStream写回相应信息，参数body指示是否在响应信息中加入在浏
            览器中显示的超文本页面内容提示作为错误*/
    private void errorResponse(DataOutputStream oStream, String code,
                               String extra, boolean body, String info)
            throws IOException {
        oStream.write((code + CRLF).getBytes());
        oStream.write(("Server: MiniHttpServer/" + CRLF).getBytes());
        if (extra != null)
            oStream.write((extra + CRLF).getBytes());
        oStream.write(("Connection: close" + CRLF).getBytes());
        if (body) {
            //写入可以在浏览器中显示的超文本错误信息
            oStream.write((CRLF	+ "<html><head><title>"	+ code
                    + "</title></head><body>"	+ "<h2>MiniHTTP server "		+ "</h2>"
                    + "<p>"	+ code + "</p>"	+ "<p>"	+ info	+ "</p>"
                    + "</body></html>")	.getBytes());
        }
    }
    //返回关于Socket的信息
    private String getSocketInfo() {
        StringBuffer sb = new StringBuffer(196);
        if (sock == null)
            return "";
        sb.append(sock.getInetAddress().getHostAddress());
        sb.append(":").append(sock.getPort());
        sb.append(" -> ");
        sb.append(sock.getLocalAddress().getHostAddress());
        sb.append(":").append(sock.getLocalPort());
        return sb.toString();
    }
}
