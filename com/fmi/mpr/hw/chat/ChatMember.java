import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class ChatMember {
    public static String memberName;
    public static boolean isRunning = true;

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.print("Enter your name: ");
        memberName = in.nextLine();

        int port = 8888;
        MulticastSocket multicastSocket = new MulticastSocket(port);
        InetAddress groupAddress = InetAddress.getByName("230.0.0.1");

        multicastSocket.joinGroup(groupAddress);

        Thread threadForReceiving = new Thread(new ThreadForReceiving(groupAddress, port, multicastSocket));
        threadForReceiving.start();

        System.out.println("Welcome to the group chat! You can now send texts, images or videos.");

        while(true){
            String textMessage = in.nextLine();

            if(!textMessage.equals("end")){
                textMessage = memberName + ": " + textMessage;
                byte[] messageInBytes = textMessage.getBytes();

                DatagramPacket datagramPacket = new DatagramPacket(messageInBytes,
                        messageInBytes.length, groupAddress, port);
                multicastSocket.send(datagramPacket);
            }
            else{
                isRunning = false;
                System.out.println("Bye!");

                multicastSocket.leaveGroup(groupAddress);
                multicastSocket.close();

                break;
            }
        }
    }
}
