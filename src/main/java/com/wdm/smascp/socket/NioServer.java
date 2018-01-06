package com.wdm.smascp.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * @author wdmyong 2018/01/05
 * single thread
 * 纯线程名都是main
 * learning...
 */
public class NioServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(8096));
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        Handler handler = new Handler(1024);
        while (true) {
            if (selector.select(3000) == 0) {
                printWithThreadNameAndTime("waiting for request...");
                continue;
            }
            printWithThreadNameAndTime("deal with request...");
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                try {
                    if (key.isAcceptable()) {
                        handler.handleAccept(key);
                    } else if (key.isReadable()) {
                        handler.handleRead(key);
                    }
                } catch (IOException e) {
                    keyIterator.remove();
                    continue;
                }
                keyIterator.remove();
            }

        }
    }

    private static void printWithThreadNameAndTime(String msg) {
        String name = Thread.currentThread().getName();
        LocalDateTime now = LocalDateTime.now();
        System.out.println(name + "\t" + msg + "\t" + now);
    }

    private static class Handler {
        private int bufferSize = 1024;
        private String localCharset = "UTF-8";

        public Handler() {
        }

        public Handler(int bufferSize) {
            this(bufferSize, null);
        }

        public Handler(String localCharset) {
            this(-1, localCharset);
        }

        public Handler(int bufferSize, String localCharset) {
            if (bufferSize > 0) {
                this.bufferSize = bufferSize;
            }
            if (localCharset != null) {
                this.localCharset = localCharset;
            }
        }

        public void handleAccept(SelectionKey key) throws IOException {
            SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
            sc.configureBlocking(false);
            sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
        }

        public void handleRead(SelectionKey key) throws IOException {
            SocketChannel sc = (SocketChannel)key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            buffer.clear();
            if (sc.read(buffer) == -1) {
                sc.close();
            } else {
                buffer.flip();
                String receivedData = Charset.forName(localCharset).newDecoder().decode(buffer).toString();
                printWithThreadNameAndTime(receivedData);
                String sendData = "return same data: " + receivedData;
                buffer = ByteBuffer.wrap(sendData.getBytes(localCharset));
                sc.write(buffer);
                sc.close();
            }
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public String getLocalCharset() {
            return localCharset;
        }

        public void setLocalCharset(String localCharset) {
            this.localCharset = localCharset;
        }
    }
}
