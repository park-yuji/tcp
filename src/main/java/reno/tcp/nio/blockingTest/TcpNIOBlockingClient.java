package reno.tcp.nio.blockingTest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class TcpNIOBlockingClient {
    public static void main(String[] args) {
        SocketChannel socketChannel = null; // 클라이언트 용
        InetAddress ia = null;

        try{
            // 병령처리 로직 > 수천 개의 클라이언트가 동시에 연결되면 수천 개의 스레드가 서버에 생성되기 때문에 서버 성능이 급격히 저하되고, 다운되는 현상이 발생
            // 해결 > 스레드풀 사용
            socketChannel = SocketChannel.open(); // SocketChannel = 스레드
            socketChannel.configureBlocking(true);

            System.out.println("[연결 요청]");
            ia = InetAddress.getLocalHost();
            socketChannel.connect(new InetSocketAddress(ia,15001));
            System.out.println("[연결 성공]");

            ByteBuffer byteBuffer = null;
            Charset charset = Charset.forName("UTF-8");

            byteBuffer = charset.encode("Hello Server");
            socketChannel.write(byteBuffer);  // 문자열 보내기
            System.out.println("[데이터 보내기 성공]");

            byteBuffer = ByteBuffer.allocate(100);

            try {

                int byteCount = socketChannel.read(byteBuffer); // 문자열 데이터를 받기 위해

                if (byteCount == -1) {
                    throw new IOException();
                }

            } catch (Exception e) {
                try {
                    socketChannel.close();
                } catch (Exception e2) { }
            }

            byteBuffer.flip();

            String data = charset.decode(byteBuffer).toString();
            System.out.println("[데이터 받기 성공] :" + data);


        } catch (Exception e){
            e.printStackTrace();
        }

        if(socketChannel.isOpen()){
            try {
                socketChannel.close();
            }catch (IOException el){
                el.printStackTrace();
            }
        }


    }


}
