package com.example.demo.javaSrc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {
    @Value("${db.password}")
    private static String password;

    public static String getPassword() {
        return password;
    }
}
