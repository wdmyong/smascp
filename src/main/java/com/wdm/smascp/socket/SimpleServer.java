package com.wdm.smascp.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.threads.TaskThreadFactory;

/**
 * @author wdmyong 2018/01/05
 */
public class SimpleServer {
    public static void main(String[] args) {
        // singleThread();
        multiThread();
    }

    private static void singleThread() {
        try {
            ServerSocket server = new ServerSocket(8090);
            while (true) {
                accept(server);
            }
            //server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void multiThread() {
        try {
            ServerSocket server = new ServerSocket(8090);
            ExecutorService executorService = Executors.newFixedThreadPool(3,
                    new TaskThreadFactory("myThread-", true, 10));
            executorService.submit(() -> {
                while (true) {
                    accept(server);
                }
            });
            executorService.submit(() -> {
                while (true) {
                    accept(server);
                }
            });
            executorService.submit(() -> {
                while (true) {
                    accept(server);
                }
            });
            executorService.awaitTermination(1, TimeUnit.DAYS);
            //server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void accept(ServerSocket server) {
        try {
            // 监听与处理的是同一线程
            Socket socket = server.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();
            String name = Thread.currentThread().getName();
            LocalTime localTime = LocalTime.now();
            System.out.println(name + " received from client: " + line + "\t" + localTime);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.println(name + " already receive data: " + line + "\t" + localTime);
            pw.flush();
            pw.close();
            br.close();
            socket.close();
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
