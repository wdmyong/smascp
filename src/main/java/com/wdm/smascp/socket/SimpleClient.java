package com.wdm.smascp.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.threads.TaskThreadFactory;

/**
 * @author wdmyong 2018/01/05
 */
public class SimpleClient {
    public static void main(String[] args) throws Exception {
        connect();
        ExecutorService executorService = Executors.newFixedThreadPool(4,
                new TaskThreadFactory("myThread-", true, 10));
        executorService.submit(() -> connect());
        //Thread.sleep(10);
        executorService.submit(() -> connect());
        //Thread.sleep(10);
        executorService.submit(() -> connect());
        executorService.submit(() -> connect());
        executorService.submit(() -> connect());
        executorService.submit(() -> connect());
        executorService.submit(() -> connect());
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    private static void connect() {
        String msg = Thread.currentThread().getName() + " Hello world, my Server...";
        try {
            Socket socket = new Socket("127.0.0.1", 8090);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.println(msg);
            pw.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();
            System.out.println("received from server: " + line);
            pw.close();
            br.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
