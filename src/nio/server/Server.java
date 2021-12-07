package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class Server {
    private static final int BUFFER = 1024;
    private ByteBuffer readBuffer   = ByteBuffer.allocate(BUFFER);
    private ByteBuffer writeBuffer  = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    private int        port;

    public Server( int port ){
        this.port = port;
    }

    public void start(){
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()){

            serverChannel.configureBlocking(false);
            serverChannel.socket().bind( new InetSocketAddress( port ));
            serverChannel.register( selector , SelectionKey.OP_ACCEPT);
            System.out.println("Server Start,The Port is:"+port);

            while ( true ){
                if(selector.select()>0){
                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                    for ( SelectionKey key : selectionKeySet ){
                        serverHandle( key ,selector );
                    }
                    selectionKeySet.clear();
                }
            }


        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void serverHandle( SelectionKey key , Selector selector )throws IOException{
        //客户端连接
        if( key.isAcceptable() ){
            ServerSocketChannel server = (ServerSocketChannel)key.channel();

            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register( selector ,SelectionKey.OP_READ );
            System.out.println("客户端[" + client.socket().getPort() + "]上线啦！");

        }
        //客户端发送消息
        if( key.isReadable() ){
            SocketChannel client = (SocketChannel)key.channel();

            String msg = receiveMsg( client );
            System.out.println("客户端[" + client.socket().getPort() + "]: "+msg);

            sendMsg( msg ,client ,selector );

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

    public void sendMsg( String msg ,SocketChannel client ,Selector selector )throws IOException{
        msg = "客户端[" + client.socket().getPort() + "]: "+msg;
        for( SelectionKey key : selector.selectedKeys() ){
            //判断key有效，key不等于当前客户端，不等于服务器
            if( key.isValid() && !client.equals( key.channel() ) && !(key.channel() instanceof ServerSocketChannel) ){
                SocketChannel clientchannel = (SocketChannel)key.channel();//获取客户端socket channel

                writeBuffer.clear();
                writeBuffer.put(charset.encode(msg));
                writeBuffer.flip();

                while ( writeBuffer.hasRemaining() ){
                    clientchannel.write( writeBuffer );
                }
            }
        }
    }

    public static void main(String[] args) {
        new Server(8088).start();
    }
}
