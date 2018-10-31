import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

class server
{
    //vector to save list of active users
    static Vector<ClientHandler> listOfActiveUsers = new Vector<>();

    //ArrayLists to save the messages of offline users
    static ArrayList<String> from = new ArrayList<>();
    static ArrayList<String> to = new ArrayList<>();
    static ArrayList<String> message = new ArrayList<>();

    public static void main(String[] args) throws java.io.IOException
    {
        //create a server socket
        ServerSocket ss = new ServerSocket(5000);

        //loop to continuously listen and accept the client connections
        while (true) {

            // create a socket and accept client connection
            final Socket sock = ss.accept();
            DataInputStream din = new DataInputStream(sock.getInputStream());
            DataOutputStream dout = new DataOutputStream(sock.getOutputStream());

            //set a limit of active users to avoid overload in server, here limit is set to 10
            if(listOfActiveUsers.size()>10) {
                dout.writeUTF("Server Overload");
                sock.close();
                continue;
            }
            //if active users are less than limit(10), start a new thread for user
            ClientHandler obj = new ClientHandler(sock,din,dout);
            new Thread(obj).start();
        }
    }
}
class ClientHandler implements Runnable
{
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;

    // constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }

    @Override
    public void run() {
        try{
            System.out.println("new client request");

            //ask for a common password of server
            dos.writeUTF("connected to server\nEnter password");
            dos.flush();
            String pass = dis.readUTF();

            //if password is wrong, close the socket and wait for another connection
            if (!pass.equals("1")) {
                dos.writeUTF("wrong password");
                this.s.close();
                return;
            }

            //if password matches ask the username
            dos.writeUTF("enter name");
            name = dis.readUTF();
            if (server.listOfActiveUsers.size()>10)
            {
                dos.writeUTF("server overload");
                this.s.close();
                return;
            }

            //add the user to list of active users
            server.listOfActiveUsers.add(this);

            dos.writeUTF("--start chatting!! format :NameOfRecipient_Message , for file: NameOfRecipient_file_Message--");
            System.out.println(name+" is active, Active Clients= "+server.listOfActiveUsers.size());

            //check for the messages in ArrayList meant for this username
            if(server.message.size()>0)
            {
                for(int i=0;i<server.message.size();i++){
                    String a = server.to.get(i);
                    if(a.equals(name)) {
                        dos.writeUTF(server.from.get(i)+":" +server.message.get(i));

                        //after displaying the message delete it so that it does'nt get displayed again and again.
                        server.to.remove(i);
                        server.message.remove(i);
                        server.from.remove(i);
                        i--;
                    }
                }
            }

            //loop to continuously read and write messages
            while (true)
            {
                // receive the string
                String received = dis.readUTF();

                // if user sends the msg logout, then remove it from list of active users and close the socket
                if(received.equals("logout")){
                    server.listOfActiveUsers.remove(this);
                    dos.writeUTF("loggedOut");
                    System.out.println(this.name+" is loggedout ,ActiveUsers=" + server.listOfActiveUsers.size());
                    this.s.close();
                    break;
                }

                //separate the name of recipient from msg
                String[] sr = received.split("_");
                String x = sr[0];

                //only if the user typed the message in right format then forward it otherwise discard
                if(sr.length>=2){
                    String y = sr[1];
                    int f=0;
                    for (ClientHandler mc : server.listOfActiveUsers)
                    {
                        if (mc.name.equals(x))
                        {
                            mc.dos.writeUTF(this.name+" : "+y);
                            f=1;
                            break;
                        }
                    }
                    //if no active user found of this name then save it in ArrayList
                    if(f==0){
                        server.from.add(this.name);
                        server.to.add(x);
                        server.message.add(y);
                    }
                }
            }
        }

        catch (IOException e){
            e.getMessage();
            server.listOfActiveUsers.remove(this);
            System.out.println(this.name+" is loggedout ,ActiveUsers=" + server.listOfActiveUsers.size());
        }
    }
}