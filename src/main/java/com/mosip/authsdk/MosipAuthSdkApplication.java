package com.mosip.authsdk;

import com.mosip.authsdk.examples.DemoAuth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MosipAuthSdkApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoAuth.class, args);
    }
}
