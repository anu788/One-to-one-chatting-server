import java.io.*;
import java.net.Socket;

public class client {
    public static void main(String[] args) throws java.io.IOException{
        Socket sock = new Socket("127.0.0.1",5000);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        DataInputStream din = new DataInputStream(sock.getInputStream());
        DataOutputStream dout = new DataOutputStream(sock.getOutputStream());

        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    try {
                        String msg = br.readLine();
                        dout.writeUTF(msg);
                        dout.flush();
                    }
                    catch (java.io.IOException e) {
                        e.getMessage();
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client
                        String msg = din.readUTF();
                        System.out.println(msg);
                        if(msg.equals("loggedOut") || msg.equals("wrong password")){
                            System.out.println("connection closed!!!");
                            return;
                        }
                    } catch (java.io.IOException e) {

                        e.getMessage();
                    }
                }
            }
        });
        readMessage.start();
        sendMessage.start();
    }
}
