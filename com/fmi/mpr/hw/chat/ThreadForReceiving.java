import java.io.File;
import java.io.FileOutputStream;
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

                if(message.startsWith("#FILE#")){
                    String[] arr = message.trim().split(" ");
                    String fileName = arr[1], memberName = arr[2];

                    if(!memberName.equals(ChatMember.memberName)) {
                        receiveFile(fileName, multicastSocket, true);
                    }
                    else{
                        receiveFile(fileName, multicastSocket, false);
                    }
                }
                else if (!message.startsWith(ChatMember.memberName)) {
                    System.out.println(message);
                }

            } catch (IOException e) {
                //System.out.println("Socket closed!");
            }
        }
    }

    private void receiveFile(String fileName, MulticastSocket multicastSocket, boolean createFile) throws IOException {
        //String[] arr = fileName.split("\\.");
        fileName = fileName.split("\\.")[0];
        //String name = arr[0], format = arr[1];

        //System.out.println("file name = " + name + ", format = " + format);

        File file = new File(fileName + "_copy_" + ChatMember.memberName + ".jpg");
        //File file = new File(name + "_copy_" + ChatMember.memberName + "." + format);
        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        boolean fileSent = false;

        while(!fileSent){
            DatagramPacket packet = new DatagramPacket(buffer, 4096);
            multicastSocket.receive(packet);
            int bytesReceived = packet.getLength();
            byte[] data = packet.getData();

            if(new String(data, 0, bytesReceived).trim().equals("#SENT#")){
                fileSent = true;
            }
            else if(createFile){
                out.write(packet.getData(), 0, bytesReceived);
            }
        }

        System.out.println("Done.");
        out.flush();

        if(out != null){
            out.close();
        }
    }
}
