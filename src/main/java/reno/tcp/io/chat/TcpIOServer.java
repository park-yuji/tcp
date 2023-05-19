package reno.tcp.io.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpIOServer {

    public static void main(String[] args) throws IOException{
        Socket socket = null;

        User user = new User();

        ServerSocket server_socket = null;

        int count = 0;
        Thread thread[] = new Thread[10];

        System.out.println("관리자님 어서오세요.");
        try {
            server_socket = new ServerSocket(17779);

            while(true){
                socket = server_socket.accept();

                thread[count] = new Thread(new Reciever(user,socket));
                thread[count].start();
                count++;
            }


        } catch (Exception e){}

        if(!socket.isClosed()){
            socket.close();
        }

    }

}
