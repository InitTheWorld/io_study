package aio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedSelectorException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {
    private static final int BUFFER = 1024;
    private int              DEFAULT_SERVER_PORT = 8088;
    private String           DEFAULT_SERVER_HOST = "127.0.0.1";
    private ByteBuffer       buffer              = ByteBuffer.allocate(BUFFER);
    private Charset          charset             = Charset.forName("UTF-8");
    private ExecutorService  executorservice     = Executors.newSingleThreadExecutor();
    private AsynchronousSocketChannel clientChannel;

    public void start() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT));
            future.get();

            executorservice.execute( new ClientHandler( this ) );

            while ( true ){
                Future<Integer> read=clientChannel.read(buffer);
                if(read.get()>0){
                    buffer.flip();
                    String msg=String.valueOf(charset.decode(buffer));
                    System.out.println(msg);
                    buffer.clear();
                }else {
                    //如果read的结果小于等于0说明和服务器连接出现异常
                    System.out.println("服务器断开连接");
                    if(clientChannel!=null){
                        clientChannel.close();
                    }
                    System.exit(-1);
                }
            }

        }catch ( IOException e){
            e.printStackTrace();
        }catch ( ClosedSelectorException e ){

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(String msg) {
        if (msg.isEmpty())
            return;
        ByteBuffer buffer = charset.encode(msg);
        Future<Integer> write=clientChannel.write(buffer);
        try {
            //获取发送结果，如果get方法发生异常说明发送失败
            write.get();
        } catch (ExecutionException|InterruptedException e) {
            System.out.println("消息发送失败");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new Client().start();
    }

}
