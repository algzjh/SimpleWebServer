package BackEnd;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

public class Attachment {
    public AsynchronousServerSocketChannel channelServer;
    public AsynchronousSocketChannel channelClient;
    public boolean isReadMode;
    public ByteBuffer buffer;
    // get; put; bulk get; bulk put; view buffers;
    // compacting, duplicating, slicing
    public SocketAddress clientAddr;
}
