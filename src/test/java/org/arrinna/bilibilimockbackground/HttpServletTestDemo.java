package org.arrinna.bilibilimockbackground;

import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class HttpServletTestDemo {

    public static Map<String,String> getHeaderMap(){
        Map<String,String> hashMap=new HashMap<>();
        hashMap.put("accessKey","123456789");
        hashMap.put("secretKey","tianguancifu");
        return hashMap;
    }


    public static void main(String[] args) {
        Map<String,String> str=getHeaderMap();
        String content=str.toString()+"."+"Judy123456789";
        System.out.println(content);
    }
}
