package reno.tcp.io.test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPIOSocketTestClient {

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader in_server = null;
        BufferedReader in_key = null;

        PrintWriter out = null;
        InetAddress ia = null;

        try {
            ia = InetAddress.getLocalHost();

            socket = new Socket(ia, 17777);
            in_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            in_key = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

            System.out.println(socket.toString());

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println("서버로 보낼 메세지 : ");
            String data = null;

            data = in_key.readLine();

            out.println(data);
            out.flush();

            String str2 = in_server.readLine();
            System.out.println("서버로 부터 되돌아온 메세지 : " + str2);

            out.close();
            in_key.close();
            in_server.close();
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
