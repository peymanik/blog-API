package com.peyman.blogapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.security.*;
import java.util.Arrays;
import java.util.Base64;

@EnableJpaAuditing
@SpringBootApplication
@EnableAsync
public class BlogApiApplication {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();

        // Extract CORRECT private key bytes (32 bytes after 16-byte header)
        byte[] privateKeyBytes = kp.getPrivate().getEncoded();
        byte[] rawPrivateKey = Arrays.copyOfRange(privateKeyBytes, 16, 48);

        // Extract public key (last 32 bytes)
        byte[] publicKeyBytes = kp.getPublic().getEncoded();
        byte[] rawPublicKey = Arrays.copyOfRange(publicKeyBytes, publicKeyBytes.length - 32, publicKeyBytes.length);

        System.out.println("Private: " + Base64.getEncoder().encodeToString(rawPrivateKey));
        System.out.println("Public: " + Base64.getEncoder().encodeToString(rawPublicKey));
        var app = SpringApplication.run(BlogApiApplication.class, args);
        String activeProfile = app.getEnvironment().getProperty("spring.profiles.active");
        String port = app.getEnvironment().getProperty("management.server.port");
        String name = app.getEnvironment().getProperty("spring.application.name");
        String address = app.getEnvironment().getProperty("server.address", "localhost");
        String DB = app.getEnvironment().getProperty("spring.datasource.username");

        String GREEN = "\u001B[1;32m";
        String CYAN = "\u001B[1;36m";
        String RESET = "\u001B[0m";

        System.out.println();
        System.out.println(GREEN + "=====================================================================================" + RESET);
        System.out.println(GREEN + " 🟢 " + CYAN + name + "     " + address + "   " + port );
        System.out.println(GREEN + " 🟢 " + CYAN + "Profile:    " + GREEN + activeProfile + RESET);
        System.out.println(GREEN + "=====================================================================================" + RESET);
        System.out.println();



        //show port, ip, active profiles, application name, and ....
    }

}
