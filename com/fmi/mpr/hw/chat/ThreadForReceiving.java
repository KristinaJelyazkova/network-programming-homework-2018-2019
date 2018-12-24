import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class ThreadForReceiving implements Runnable{
    private InetAddress groupAddress;
    private int port;
    private MulticastSocket multicastSocket;

    public ThreadForReceiving(InetAddress groupAddress, int port, MulticastSocket multicastSocket) {
        this.groupAddress = groupAddress;
        this.port = port;
        this.multicastSocket = multicastSocket;
    }

    @Override
    public void run() {
        while(ChatMember.isRunning){
            byte[] messageInBytes = new byte[4096];

            DatagramPacket datagramPacket = new DatagramPacket(messageInBytes,
                    messageInBytes.length, groupAddress, port);


            try {
                multicastSocket.receive(datagramPacket);
                String message = new String(messageInBytes, 0,
                        datagramPacket.getLength(), StandardCharsets.UTF_8);

                if (!message.startsWith(ChatMember.memberName)) {
                    System.out.println(message);
                }

            } catch (IOException e) {
                System.out.println("Socket closed!");
            }
        }
    }
}
