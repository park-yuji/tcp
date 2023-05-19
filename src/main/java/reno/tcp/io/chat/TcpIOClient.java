package reno.tcp.io.chat;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TcpIOClient {
    public static void main(String[] args) {
        Socket socket = null;
        DataInputStream in_server = null;
        BufferedReader in_keyboard = null;

        DataOutputStream out = null;
        InetAddress ia = null;

        try {
            ia = InetAddress.getLocalHost();
            socket = new Socket(ia,17779);

            in_server = new DataInputStream(socket.getInputStream());
            in_keyboard = new BufferedReader(new InputStreamReader(System.in));

            out = new DataOutputStream(socket.getOutputStream());

            System.out.print("닉네임을 입력해 주세요 : ");
            String data = in_keyboard.readLine();

            out.writeUTF(data);
            Thread th = new Thread(new Send(out));
            th.start();

        }catch (IOException e){}

        try{
            while (true){
                String str2 = in_server.readUTF();
                System.out.println(str2);
            }
        }catch (IOException e){}

        if(!socket.isClosed()){
            try{
                out.close();
                in_keyboard.close();
                in_server.close();
                socket.close();
            }catch (IOException e){

            }

        }

    }
}
