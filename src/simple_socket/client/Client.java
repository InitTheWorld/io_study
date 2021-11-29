package simple_socket.client;

import java.io.*;
import java.net.Socket;


public class Client {
    public static void main(String args[]){
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        final int DEFAULT_SERVER_PORT = 8088;

        /* public Socket(String host, int port)
        throws UnknownHostException, IOException*/
        try(Socket socket = new Socket( DEFAULT_SERVER_HOST , DEFAULT_SERVER_PORT ) ){
            System.out.println( " connection success" );
            BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
            BufferedReader userreader = new BufferedReader( new InputStreamReader(System.in) );
            String msg = null ;
            while( true ){
                String input = userreader.readLine();
                writer.write( input + "\n" );
                writer.flush();

                msg = reader.readLine();
                System.out.println( "[from server] : " + msg );
                if( msg .equals("quit") ){
                    System.out.println( "client offline" );
                    break;
                }
            }
        }catch ( IOException e){
            e.printStackTrace();
        }
    }
}
