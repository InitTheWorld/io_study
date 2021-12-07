package aio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int BUFFER = 1024;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private AsynchronousChannelGroup        channelGroup;

    //处理并发
    private CopyOnWriteArrayList<ServerClientHandler> userHandlerList;

    private Charset charset = Charset.forName("UTF-8");

    private String     host;
    private int        port;

    public Server( String host ,int port ){
        this.host = host;
        this.port = port;
        userHandlerList = new CopyOnWriteArrayList<>();
    }

    public void start(){
        try{
            ExecutorService executorService = Executors.newFixedThreadPool( 10 );
            channelGroup = AsynchronousChannelGroup.withThreadPool( executorService );

            serverSocketChannel = AsynchronousServerSocketChannel.open( channelGroup );
            serverSocketChannel.bind( new InetSocketAddress( host , port ));
            System.out.println("Server Start,The Port is:"+port);

            while ( true ){
                serverSocketChannel.accept( null,new AcceptHandler() );
                System.in.read();
            }
        }catch (IOException e ){
            e.printStackTrace();
        }finally {
            if(serverSocketChannel!=null){
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel,Object>{
        @Override
        public void completed( AsynchronousSocketChannel clientChannel, Object attachment ) {
            if( serverSocketChannel.isOpen() ){
                serverSocketChannel.accept( null ,this );
            }

            if( clientChannel!=null && serverSocketChannel.isOpen() ){
                ServerClientHandler serverClientHandler =new ServerClientHandler(clientChannel);
                userHandlerList.add(serverClientHandler);
                System.out.println( getPort(clientChannel) + "上线啦！");
                ByteBuffer buffer=ByteBuffer.allocate(BUFFER);

                clientChannel.read(buffer,buffer,serverClientHandler);

            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败"+exc);
        }
    }

    public class ServerClientHandler implements CompletionHandler<Integer, ByteBuffer>{
        private AsynchronousSocketChannel clientChannel;
        public ServerClientHandler( AsynchronousSocketChannel clientChannel ){
            this.clientChannel = clientChannel;
        }
        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if( buffer != null ){
                if( result < 0 ){
                    removeClient( this );
                }else {
                    buffer.flip();
                    String msg = String.valueOf( charset.decode(buffer) );
                    System.out.println(getPort(clientChannel)+msg);

                    sendMessage( clientChannel,msg );
                    buffer=ByteBuffer.allocate(BUFFER);

                    if( msg.equals("quit") ){
                        removeClient( this );
                    }else {
                        clientChannel.read( buffer, buffer, this );
                    }
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            System.out.println("客户端读写异常："+exc);
        }
    }

    private String getPort(AsynchronousSocketChannel clientChannel){
        try {
            InetSocketAddress address=(InetSocketAddress)clientChannel.getRemoteAddress();
            return "客户端["+address.getPort()+"]:";
        } catch (IOException e) {
            e.printStackTrace();
            return "客户端[Undefined]:";
        }
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel,String msg){
        for(ServerClientHandler handler:userHandlerList){
            if(!handler.clientChannel.equals(clientChannel)){
                ByteBuffer buffer=charset.encode(msg);
                //write不需要buffer当辅助参数，因为写到客户端的通道就完事了，而读还需要回调函数转发给其他客户端。
                handler.clientChannel.write(buffer,null,handler);
            }
        }
    }
    //移除客户端
    private void removeClient(ServerClientHandler handler){
        userHandlerList.remove(handler);
        System.out.println(getPort(handler.clientChannel)+"断开连接...");
        if(handler.clientChannel!=null){
            try {
                handler.clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Server("127.0.0.1",8088).start();
    }
}
