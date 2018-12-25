import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
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
            System.out.println("Enter \"TEXT\" if you will send a text message to the group"
                        + "or \"FILE\" if you will send image or a video to the group.");
            System.out.println("If you enter something else, it is assumed you will send a text message.");
            String type = in.nextLine();

            if(type.equals("FILE")){
                System.out.print("Enter file name: ");
                String fileName = in.nextLine();

                sendFile(fileName, groupAddress, port, multicastSocket);
            }
            else {
                String textMessage = in.nextLine();

                if (!textMessage.equals("end")) {
                    textMessage = memberName + ": " + textMessage;
                    byte[] messageInBytes = textMessage.getBytes();

                    DatagramPacket datagramPacket = new DatagramPacket(messageInBytes,
                            messageInBytes.length, groupAddress, port);
                    multicastSocket.send(datagramPacket);
                } else {
                    isRunning = false;
                    System.out.println("Bye!");

                    multicastSocket.leaveGroup(groupAddress);
                    multicastSocket.close();

                    break;
                }
            }
        }
    }

    private static void sendFile(String fileName, InetAddress groupAddress, int port, MulticastSocket multicastSocket)
            throws IOException {
        String message = "#FILE# " + fileName + " " + ChatMember.memberName;
        byte[] messageInBytes = message.getBytes();

        DatagramPacket datagramPacket = new DatagramPacket(messageInBytes,
                messageInBytes.length, groupAddress, port);
        multicastSocket.send(datagramPacket);

        File file = new File(fileName);
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead = 0;

        while((bytesRead = in.read(buffer, 0, 4096)) > 0){
            DatagramPacket packet = new DatagramPacket(Arrays.copyOfRange(buffer, 0, bytesRead),
                    bytesRead, groupAddress, port);

            multicastSocket.send(packet);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("DONE!");
        in.close();

        message = "#SENT#";
        messageInBytes = message.getBytes();

        DatagramPacket datagramPacket1 = new DatagramPacket(messageInBytes,
                messageInBytes.length, groupAddress, port);
        multicastSocket.send(datagramPacket1);
    }
}
