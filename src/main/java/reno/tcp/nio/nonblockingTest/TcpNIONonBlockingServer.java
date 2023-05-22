package reno.tcp.nio.nonblockingTest;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TcpNIONonBlockingServer {
    public static void main(String[] args) {

        // 연결된 클라이언트를 관리할 커넥션
        // 모든 클라이언트의 소켓채널을 모아둠, 사용자가 들어오고 나가면 다른사용자들에게 알려줘야하기 때문에
        // 현재 접속되어 있는 모든 사용자의 채널정보를 가지고 있다가 이벤트가 발생하면 뿌려주는 용도.
        Set<SocketChannel> allClient = new HashSet<>();

        try(ServerSocketChannel serverSocket = ServerSocketChannel.open()){

            /* ***************************클라이언트가 접속할 수 있는 서버 준비**************************** */
            // 서비스 포트 설정 및 논블로킹 모드로 설정
            serverSocket.bind(new InetSocketAddress(15003));
            serverSocket.configureBlocking(false);

            // 채널을 관리할 Selector 생성 및 서버 소켓 채널 등록
            // 서버 소켓 채널 : 서비스 포트에 접속하려는 클라이언트 접속 요청을 받아주는 역할 (on_accept 모드로 등록)
            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT/* | SelectionKey.OP_WRITE*/);

            System.out.println("-------서버 접속 준비 완료 ---------");
            // 버퍼의 모니터 출력을 위한 출력 채널 생성

            // 입출력 시 사용할 바이트버퍼 생성
            ByteBuffer inputBuf = ByteBuffer.allocate(1024); // jvm의 힙영역에 버퍼 할당
            ByteBuffer outputBuf = ByteBuffer.allocate(1024);
            /* ***************************클라이언트가 접속할 수 있는 서버 준비**************************** */

            // 셀렉터가 서버 소켓 채널의 이벤트를 감지하도록 select() 메소드 실행
            // 이벤트 생길 시 Selector가 가지고 있는 SelectionKey들 중 이벤트가 발생한 채널의 객체만 모아둔 Set을 받음
            while(true){
                // 이벤트 발생할 때 까지 스레드 대기
                selector.select();

                // 발생한 이벤트들을 모두 Iterator에 담아줌
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                // 발생한 이벤트들을 담은 Iterator의 이벤트를 하나씩 순서대로 처리
                while(iterator.hasNext()){
                    // 현재 순서의 처리할 이벤트를 임시저장하고 Iterator 에서 지워줌
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    // 연결 요청중인 클라이언트를 처리할 조건문 작성
                    if(key.isAcceptable()){
                        // 연결 요청중인 이벤트이므로 해당 요청에 대한 소켓 채널을 생성해줌
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel clientSocket = server.accept();

                        // Selecctor 의 관리를 받기 위해서 논블로킹 채널로 바꿔줌
                        clientSocket.configureBlocking(false);

                        // 연결된 클라이언트를 컬렉션에 추가
                        allClient.add(clientSocket);

                        // 아이디를 입력받기 위한 출력을 해당 채널에 해줌
                        clientSocket.write(ByteBuffer.wrap("아이디를 입력해주세요 :".getBytes()));

                        // 아이디를 입력받을 차례이므로 읽기모드로 셀렉터에 등록
                        clientSocket.register(selector, SelectionKey.OP_READ, new ClientInfo());

                    // 읽기 이벤트(클라이언트 -> 서버)가 발생한 경우
                    }else if(key.isReadable()){

                        // 현재 채널 정보를 가져옴(attach된 사용자 정보도 가져옴)
                        SocketChannel readSocket = (SocketChannel) key.channel();
                        ClientInfo info = (ClientInfo) key.attachment();

                        // 채널에서 데이터를 읽어옴
                        try{
                            readSocket.read(inputBuf);

                        // 만약 클라이언트가 연결을 끊었다면 예외가 발생하므로 처리
                        }catch(Exception e){
                            key.cancel(); // 현재 SelectionKey를 셀렉터 관리대상에서 삭제
                            allClient.remove(readSocket); // Set에서도 삭제

                            // 서버에 종료 메세지 출력
                            String end = info.getID() + "님의 연결이 종료되었습니다.\n";
                            System.out.println(end);

                            // 자신을 제외한 클라이언트에게 종료 메세지 출력
                            outputBuf.put(end.getBytes());
                            for(SocketChannel s: allClient){
                                if(!readSocket.equals(s)){
                                    outputBuf.flip();
                                    s.write(outputBuf);
                                }
                            }
                            outputBuf.clear();
                            continue;
                        }

                        // 현재 아이디가 없을 경우 아이디 등록
                        if(info.isID()){
                            // 현재 inputBuf 내용 중 개행 문자를 제외하고 가져와서 ID로 넣어줌
                            inputBuf.limit(inputBuf.position() -2);
                            inputBuf.position(0);
                            byte[] b = new byte[inputBuf.limit()];
                            inputBuf.get(b);
                            info.setID(new String(b));

                            // 서버에 출력
                            String enter = info.getID() + "님이 입장하셨습니다.\n";
                            System.out.println(enter);

                            outputBuf.put(enter.getBytes());

                            // 모든 클라이언트에게 메세지 출력
                            for(SocketChannel s : allClient){
                                outputBuf.flip();
                                s.write(outputBuf);
                            }

                            inputBuf.clear();
                            outputBuf.clear();
                            continue;

                        }

                        // 읽어온 데이터와 아이디 정보를 결합해 출력한 버퍼 생성
                        inputBuf.flip();
                        outputBuf.put((info.getID() + " : ").getBytes());
                        outputBuf.put(inputBuf);
                        outputBuf.flip();

                        for(SocketChannel s : allClient){
                            if(!readSocket.equals(s)){
                                s.write(outputBuf);
                                outputBuf.flip();
                            }
                        }
                        inputBuf.clear();
                        outputBuf.clear();
                    }

                } //while문 종료

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}

// 접속한 사용자의 ID를 가진 클래스
class ClientInfo {

    // 아직 아이디 입력이 안된 경우 true
    private boolean idCheck = true;
    private String id;

    // ID가 들어있는지 확인
    boolean isID() {

        return idCheck;
    }

    // ID를 입력받으면 false로 변경
    private void setCheck() {

        idCheck = false;
    }

    // ID 정보 반환
    String getID() {

        return id;
    }

    // ID 입력
    void setID(String id) {
        this.id = id;
        setCheck();
    }
}
