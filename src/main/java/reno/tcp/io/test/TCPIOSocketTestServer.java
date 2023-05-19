package reno.tcp.io.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPIOSocketTestServer {
    public static void main(String arg[]){

        // 1. Server 측에서 ServerSocket을 생성하고 accept() 메서드 호출함으로써 Client 접속 대기
        // Client와 통신하기 위한 Socket
        Socket socket = null;
        // 서버 생성을 위한 ServerSocket
        ServerSocket server_socket = null;
        // Client로부터 데이터를 읽어들이기 위한 입력스트림
        BufferedReader in = null;
        // Client로부터 데이터를 내보내기 위한 출력스트림
        PrintWriter out = null;

        try {
            server_socket = new ServerSocket(17777);
        } catch (IOException e) {
            System.out.println("해당 포트가 열려있습니다.");
            throw new RuntimeException(e);
        }

        System.out.println("서버 오픈!!");
        try {
            socket = server_socket.accept();    //서버 생성 , Client 접속 대기
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력스트림 생성
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))); // 출력스트림 생성

            String str = null;
            str = in.readLine(); // 데이터 라인단위로 읽음

            str = "[CLIENT] : " + str;
            System.out.println(str);

            out.write(str);
            out.flush(); // 버퍼에 남아있는 데이터 출력시킴 (버퍼 비우기)

            out.close();
            in.close();
            socket.close();

            System.out.println("서버 종료!!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
