package reno.tcp.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Http_Client_Flow_Cor {

    //fixed 전문
    // public static final String PARAM = "TMAX10000000000001012345678912홍길동";

    public static final String PARAM = "000";

    public static void main(String[] args) throws JsonProcessingException {

        System.out.println("비동기 시작");
        // 현재 시간
        Date now = new Date();
        // 현재시간 출력
        System.out.println("호출 시작 시간 : "+ now);

        for(int i = 0; i < 10; i ++){
            // 변수 세팅
            String code = PARAM+i;
            int listSize = 2;
            Map<String, String> map = new HashMap<>();
            Map<String, String> map2 = new HashMap<>();
            map.put("key1","value"+i+"1");
            map.put("key2","value"+i+"2");
            map2.put("key1","value"+(i+1)+"1");
            map2.put("key2","value"+(i+1)+"2");

            BodyMessage bodyMessage = new BodyMessage();
            bodyMessage.setCode(code);
            bodyMessage.setListSize(listSize);
            bodyMessage.addList(map);
            bodyMessage.addList(map2);

            ObjectMapper objectMapper = new ObjectMapper();
            String objString = objectMapper.writeValueAsString(bodyMessage);

            System.out.println(i + "번째 스레드 시작");
            System.out.println("Parameters : " + objString);
            Task a = new Task();
            a.setParam(objString);
            Runnable task = a;

            Thread thread = new Thread(task);
            thread.start();

        }

        System.out.println("호출 끝 시간 : "+ now);

    }

    // http 통신
    public static void httpTest(String param) throws IOException {
        HttpPost post = new HttpPost("http://192.168.56.104:12222/http/totcp6");
        post.setHeader("Accept","application/json");
        post.setHeader("Content-type","application/json");
        CloseableHttpClient client = HttpClients.custom().build();

        StringEntity entity = new StringEntity(param,"EUC-KR");
        post.setEntity(entity);

        HttpResponse response = client.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Code: "+ responseCode);
        // 현재 시간
        Date now = new Date();
        // 현재시간 출력
        System.out.println("시간 : "+ now);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"euc-kr"));
        String line = "";

        while((line = rd.readLine()) != null){
            System.out.println(line);
        }

    }

    static class Task implements Runnable{

        String param;

        public void setParam(String param){
            this.param = param;
        }

        @Override
        public void run(){
            try {
                httpTest(param);
            } catch (IOException e) {
                System.out.println("통신 오류");
                throw new RuntimeException(e);
            }
        }
    }

}



@Data
class BodyMessage{
    private String code;
    private int listSize;
    private List<Map<String,String>> list = new ArrayList<>();

    void addList(Map<String,String> map){
        this.list.add(map);
    }

}