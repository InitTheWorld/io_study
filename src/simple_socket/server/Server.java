package simple_socket.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String args[]){
        final int DEFAULT_SERVER_PORT = 8088;
        try(ServerSocket serversocket = new ServerSocket( DEFAULT_SERVER_PORT ) ){
            System.out.println( "Server is listening port "+ DEFAULT_SERVER_PORT );
            while( true ){
                Socket socket = serversocket.accept();
                BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );

                String msg = null;


                while ( (msg = reader.readLine()) != null ){
                    System.out.println( " [from client] : " + msg );
                    writer.write( msg + "\n" );
                    writer.flush();
                    if( msg.equals("quit") ){
                        System.out.println( "server offline" );
                        break;
                    }
                }

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
