package bio.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int DEFAULT_SERVER_PORT = 8088;
    //port + socket TODO session
    private Map<Integer , Writer> userrecord = new ConcurrentHashMap<>();

    private ExecutorService executorservice = Executors.newFixedThreadPool( 10 );

    public void addClient( Socket socket )throws IOException
    {
        if( socket != null )
        {
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
            userrecord.put( socket.getPort() , writer );
            System.out.println("Client["+socket.getPort()+"]:Online");
        }
    }

    public void removeClient( Socket socket )throws IOException
    {
        if( socket !=null )
        {
            if (userrecord.containsKey(socket.getPort()))
            {
                userrecord.get( socket.getPort() ).close();
                userrecord.remove( socket.getPort() );
                System.out.println("Client["+socket.getPort()+"]:Offline");
            }
        }
    }

    public void sendMessage( Socket socket , String msg )throws IOException
    {
        for( Integer port : userrecord.keySet() )
        {
            Writer  writer = userrecord.get(port);
            if( msg.equals("quit") ){
                System.out.println( "server offline" );
                writer.write( "Client["+socket.getPort()+"]:Offline" + "\n" );
                break;
            }
            else
            {
                writer.write( msg + "\n" );
            }
            writer.flush();
        }
    }


    public void start ()
    {
        try(ServerSocket serversocket = new ServerSocket( DEFAULT_SERVER_PORT ) ){
            System.out.println("Server Start,The Port is:"+DEFAULT_SERVER_PORT);
            while( true ){
                Socket socket = serversocket.accept();
                executorservice.execute(new ChatHandler(this,socket));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String args[]){

        Server server = new Server();
        server.start();

    }
}
