package bio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientHandler implements Runnable{
    private Client client;
    public ClientHandler( Client client ){
        this.client = client;
    }
    @Override
    public void run() {
        try{
            BufferedReader userreader = new BufferedReader( new InputStreamReader(System.in) );
            while( true ){
                String input = userreader.readLine();
                client.sendToServer( input );
                if( input .equals("quit") ){
                    System.out.println( "client offline" );
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
