package reno.tcp.nio.blockingThreadPool;

/*import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;*/

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpNIOChatServer /* extends Application*/{

        ExecutorService executorService;  // 스레드풀
        ServerSocketChannel serverSocketChannel;
        List<Client> connections = new Vector<>();

        void startServer(){
            // 서버 시작 코드
            // 스레드 풀 생성 (CPU 코어 수에 맞게 스레드 생성 및 관리하는 ExecutorService 생성)
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            try{
                // 15002번 포트에서 클라이언트의 연결을 수락하는 ServerSocketChannel 생성
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(true);
                serverSocketChannel.bind(new InetSocketAddress(15002));

            }catch (Exception e){
                if(serverSocketChannel.isOpen()){
                    stopServer();
                }
            }

            // 연결 수락 작업을 Runnable 객체로 만들고 스레드풀의 작업 스레드로 실행
            Runnable runnable = new Runnable() {
                @Override
                public void  run(){
                    /*Platform.runLater(() -> {
                        displayText("[서버 시작]");
                        btnStartStop.setText("stop");
                    });*/

                    // ServerSocketChannel은 반복해서 클라이언트 연결 요청을 받아야함. accept() 메소드 반복 호출
                    while(true){
                        try{
                            SocketChannel socketChannel = serverSocketChannel.accept();

                            String msg = "[연결 수락 : " + socketChannel.getRemoteAddress() + ": "+ Thread.currentThread().getName()+"]";
                            Client client = new Client(socketChannel);
                            connections.add(client);

                            /*Platform.runLater(()->displayText("[연결 개수 :" + connections.size() + "]"));*/

                        }catch(Exception e){
                            if(serverSocketChannel.isOpen()){
                                stopServer();
                            }
                            break;
                        }
                    }
                }
            };

            executorService.submit(runnable);

        }

        void stopServer(){
            // 서버 종료 코드
            // 1. 연결된 모든 SocketChannel 닫기
            // 2. ServerSocketChannel 닫기
            // 3. ExecutorService 종료코드

            try{
                Iterator<Client> iterator = connections.iterator();
                while (iterator.hasNext()){
                    Client client = iterator.next();
                    client.socketChannel.close();
                    iterator.remove();
                }
                if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                    serverSocketChannel.close();
                }

                if (executorService != null && executorService.isShutdown()) {
                    executorService.shutdown();
                }

               /* Platform.runLater(()->{
                    displayText("[서버 종료]");
                    btnStartStop.setText("start");
                });*/



            }catch (Exception e){

            }

        }

        class Client{
            // 데이터 통신 코드
            SocketChannel socketChannel;

            public Client(SocketChannel socketChannel) {
                this.socketChannel = socketChannel;
                receive();
            }

            void receive() {
                // 데이터 받기
                // 스레드풀의 작업 스레드가 처리하도록 작업을 Runnable로 처리
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            try {
                                ByteBuffer byteBuffer = ByteBuffer.allocate(100);

                                // 클라이언트가 비정상 종료를 했을 경우 IOException 발생
                                int byteCount = socketChannel.read(byteBuffer);

                                // 클라이언트가 정상적으로 SocketChannel의 close()를 호출헀을 경우
                                if (byteCount == -1) {
                                    throw new IOException();
                                }

                                String msg = "[요청 처리: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";

                                /*Platform.runLater(()->displayText(msg));*/

                                byteBuffer.flip();
                                Charset charset = Charset.forName("UTF-8");
                                String data = charset.decode(byteBuffer).toString();

                                for (Client client : connections) {
                                    client.send(data);
                                }

                            } catch (Exception e) {
                                try {
                                    connections.remove(Client.this);

                                    String msg = "[클라이언트 통신 안됨: " +
                                            socketChannel.getRemoteAddress() + ": " +
                                            Thread.currentThread().getName() + "]";

                                    /*Platform.runLater(()->displayText(msg));*/
                                    socketChannel.close();
                                } catch (IOException e2) {

                                }

                                break;
                            }
                        }
                    }

                };

                executorService.submit(runnable);
            }

            void send(String data) {
                // 데이터 전송
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Charset charset = Charset.forName("UTF-8");
                            ByteBuffer byteBuffer = charset.encode(data);
                            socketChannel.write(byteBuffer);
                        } catch (Exception e) {
                            try {
                                String msg = "[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": "
                                        + Thread.currentThread().getName() + "]";

                               /* Platform.runLater(() -> displayText(msg));*/
                                connections.remove(Client.this);
                                socketChannel.close();

                            } catch (IOException e2) {

                            }
                        }
                    }
                };
                executorService.submit(runnable);
            }
        }

        // UI 생성코드
      /*  TextArea txtDisplay;
        Button btnStartStop;

        @Override
        public void start(Stage primaryStage) throws Exception {
            BorderPane root = new BorderPane();
            root.setPrefSize(500, 300);

            txtDisplay = new TextArea();
            txtDisplay.setEditable(false);
            BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
            root.setCenter(txtDisplay);

            btnStartStop = new Button("start");
            btnStartStop.setPrefHeight(30);
            btnStartStop.setMaxWidth(Double.MAX_VALUE);

            btnStartStop.setOnAction(e -> {
                if (btnStartStop.getText().equals("start")) {
                    startServer();
                } else if (btnStartStop.getText().equals("stop")) {
                    stopServer();
                }
            });

            root.setBottom(btnStartStop);

            Scene scene = new Scene(root);
            scene.getStylesheets().add("app.css");
            primaryStage.setScene(scene);
            primaryStage.setTitle("Server");
            primaryStage.setOnCloseRequest(event -> stopServer());
            primaryStage.show();

        }

        void displayText(String text) {
            txtDisplay.appendText(text + "\n");
        }

        public static void main(String[] args) {
            launch(args);
        }
*/

}
