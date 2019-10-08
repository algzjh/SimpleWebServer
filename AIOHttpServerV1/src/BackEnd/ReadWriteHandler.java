package BackEnd;

import BackEnd.Attachment;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;


public class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {
    private final static Charset CSUTF8 = Charset.forName("UTF-8");
    private final String CRLF = "\r\n";


    @Override
    public void completed(Integer result, Attachment att){
        /*
        -1 indicates that no bytes could be read because end-of-stream has been reached
        (The client may have terminated before writing data)
         */
        if(result == -1){
            try{
                att.channelClient.close(); // It closes the client socket channel
                System.out.printf("Stopped listening to client %s%n", att.clientAddr);
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            return;
        }

        if(att.isReadMode){
            att.buffer.flip(); // the buffer contents are flipped
            int limit = att.buffer.limit();
            byte bytes[] = new byte[limit]; // extract a bytes array
            att.buffer.get(bytes, 0, limit);
            String clientData = new String(bytes, CSUTF8);
            System.out.println("$$$$$1");
            System.out.printf("Client at %s sends message: %s%n",
                    att.clientAddr, clientData); // convert to a sting

            //======
            System.out.println("$$$$$2");

            System.out.println("clientData: \n" + clientData);

            String lines[] = clientData.split("\\r?\\n");
            String clientRequest = lines[0];
            System.out.println("clientRequest: " + clientRequest);

            //======

            att.isReadMode = false; // configure for write mode

            att.buffer.rewind(); // in preparation for being written
            att.buffer.put(clientRequest.getBytes(), 0, limit);
            att.channelClient.write(att.buffer, att, this);
            // the buffer contents are sent to the client
        }else{ // After a successful write operation
            att.isReadMode = true;
            att.buffer.clear();
            att.channelClient.read(att.buffer, att, this);
            // initiate a read operation to read from the client
        }

        /*
        This pattern continues until the client presents nothing more to read
        and the completion handler can terminate
         */
    }

    @Override
    public void failed(Throwable t, Attachment att){
        System.out.println("Connection with client broken");
    }

    private String getSocketInfo(){
        StringBuffer sb = new StringBuffer(196);
        return sb.toString();
    }

    private void errorResponse() throws IOException{

    }
}
