package nio.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static final int BUFFER = 1024;
    private SocketChannel    client;
    private Selector         selector;
    private int              DEFAULT_SERVER_PORT = 8088;
    private String           DEFAULT_SERVER_HOST = "127.0.0.1";
    private ByteBuffer       readBuffer          = ByteBuffer.allocate(BUFFER);
    private ByteBuffer       writeBuffer         = ByteBuffer.allocate(BUFFER);
    private Charset          charset             = Charset.forName("UTF-8");
    private ExecutorService  executorservice     = Executors.newSingleThreadExecutor();

    public void start(){
        try {
            client = SocketChannel.open();
            selector = Selector.open();

            client.configureBlocking( false );
            client.register( selector , SelectionKey.OP_CONNECT );
            client.connect(new InetSocketAddress( DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT ));

            while ( true ){
                selector.select();
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                for (SelectionKey key : selectionKeySet) {
                    clientHandle( key );
                }
                selectionKeySet.clear();
            }
        }catch ( IOException e){
            e.printStackTrace();
        }catch ( ClosedSelectorException e ){

        }
    }

    public void clientHandle( SelectionKey key )throws IOException {
        if( key.isConnectable() ){
            SocketChannel client = (SocketChannel)key.channel();

            if( client.finishConnect() ){
                executorservice.execute( new ClientHandler( this ) );
            }

            client.register(selector,SelectionKey.OP_READ);
        }
        if( key.isReadable() ){
            SocketChannel client = (SocketChannel)key.channel();

            String msg = receiveMsg( client );
            System.out.println("客户端[" + client.socket().getPort() + "]: "+msg);

            if( msg.equals("quit") ){
                key.cancel();
                selector.wakeup();
                System.out.println("客户端[" + client.socket().getPort() + "]下线啦！");
            }

        }
    }

    public String receiveMsg( SocketChannel client )throws IOException{
        String msg = null;
        readBuffer.clear();
        while ( client.read(readBuffer) > 0 );
        readBuffer.flip();
        msg = String.valueOf(charset.decode(readBuffer));
        return msg;
    }

    public void sendMsg( String msg  )throws IOException{
        if( !msg.isEmpty() ){
            writeBuffer.clear();
            writeBuffer.put(charset.encode(msg));
            writeBuffer.flip();

            while ( writeBuffer.hasRemaining() ){
                client.write( writeBuffer );
            }
            if(msg.equals("quit")){
                selector.close();
            }
        }
    }
    public static void main(String[] args) {
        new Client().start();
    }

}
