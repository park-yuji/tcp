package reno.tcp.nio.blockingTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class TcpNIOBlockingServer {
    public static void main(String[] args) {
        ServerSocketChannel serverSocketChannel = null; // 서버용

        try{
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true); // default blocking 방식, nonblocking 방식과 구분하기 위해 명시
            serverSocketChannel.bind(new InetSocketAddress(15001));

            while(true){
                System.out.println("[연결 기다림]");
                SocketChannel socketChannel = serverSocketChannel.accept(); // 이 부분에서 연결이 될때까지 블로킹
                InetSocketAddress isa = (InetSocketAddress) socketChannel.getRemoteAddress();
                System.out.println("[연결 수락함] "+ isa.getHostName());

                ByteBuffer byteBuffer = null;
                Charset charset = Charset.forName("UTF-8");

                byteBuffer = ByteBuffer.allocate(100);
                int byteCount = socketChannel.read(byteBuffer);
                byteBuffer.flip();

                String data = charset.decode(byteBuffer).toString();
                System.out.println("[데이터 받기 성공]: " + data);

                byteBuffer = charset.encode("Hello Client");
                socketChannel.write(byteBuffer);
                System.out.println("[데이터 보내기 성공]");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        if(serverSocketChannel.isOpen()){
            try{
                serverSocketChannel.close();
            }catch (IOException el){
                el.printStackTrace();
            }
        }

    }
}
