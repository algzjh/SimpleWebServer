package ClientEnd;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class Attachment {
    public AsynchronousSocketChannel channel;
    // the channel is subsequently accessed by ReadWriteHandler's completed()
    // method to perform reads and writes
    public boolean isReadMode;
    // isReadMode is a toggle that lets completed() know if it needs to
    // perform a read or a write
    public ByteBuffer buffer;
    // buffer field identified the byte buffer that's created in Client's main() method
    public Thread mainThd;
    // mainThd references a Thread object that identified main()'s thread
    // The completed() method invokes interrupt() on this reference to
    // interrupt the client so that this application can exit;
}

