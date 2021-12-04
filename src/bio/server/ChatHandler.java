package bio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatHandler implements Runnable{

    private Server server;
    private Socket socket;

    public ChatHandler ( Server server , Socket socket )
    {
        this.server = server;
        this.socket = socket;
    }
    @Override
    public void run() {
        try{
            server.addClient( socket );
            BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            String msg = null;
            while ( (msg = reader.readLine()) != null ){
                System.out.println( " [from client"+socket.getPort()+"] : " + msg );
                server.sendMessage( socket ,msg );
                if( msg.equals("quit") ){
                    System.out.println( "server "+ socket.getPort() +" offline" );
                    break;
                }
            }
            server.removeClient( socket );
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
