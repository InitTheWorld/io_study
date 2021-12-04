package bio.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private BufferedWriter  writer ;
    private BufferedReader  reader;
    private Socket          socket;
    private String          DEFAULT_SERVER_HOST = "127.0.0.1";
    private int             DEFAULT_SERVER_PORT = 8088;
    private ExecutorService executorservice     = Executors.newSingleThreadExecutor();

    public void sendToServer( String msg )throws IOException {
        if( !socket.isOutputShutdown() ){
            writer.write( msg + "\n" );
            writer.flush();
        }
    }

    public String receivemsg()throws IOException{
        String msg = null;
        if( !socket.isInputShutdown() ){
            msg = reader.readLine();
        }
        return msg;
    }

    public void start(){
        try{
            socket = new Socket( DEFAULT_SERVER_HOST , DEFAULT_SERVER_PORT );
            reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );

            executorservice.execute( new ClientHandler( this ) );

            String msg = null;
            while( (msg = receivemsg())!=null ){
                System.out.println(msg);
            }


        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if( writer!=null ){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]){
        new Client().start();
    }
}
