package com.xlf.demo.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * author: xiaoliufu
 * date:   2017/1/2.
 * description:
 */
public class EchoClientHandler implements Runnable {
    private String host;
    private int port;

    private Selector selector;
    private SocketChannel socketChannel;

    EchoClientHandler(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            try {
                doConnect();
                new Thread(new OutputHandler()).start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } catch (IOException e) {
            System.exit(1);
            e.printStackTrace();
        }
    }

    private class OutputHandler implements Runnable{

        private void doWrite() throws IOException {
            Scanner scanner=new Scanner(System.in);
            while (true){
                System.out.println("Please input what you want: ");
                while (scanner.hasNextLine()){
                    String line=scanner.nextLine();
                    socketChannel.write(ByteBuffer.wrap(line.getBytes()));
                }
            }
        }
        @Override
        public void run() {
            try {
                doWrite();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey selectionKey:selectionKeys) {
                    selector.selectedKeys().remove(selectionKey);
                    try {
                        handleKeys(selectionKey);
                    } catch (Exception e) {
                        if (selectionKey != null) {
                            selectionKey.cancel();
                            if (selectionKey.channel() != null) {
                                selectionKey.channel().close();
                            }
                        }
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void handleKeys(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isValid()) {
            if (selectionKey.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                String content="";
                while (socketChannel.read(buffer)>0){
                    socketChannel.read(buffer);
                    buffer.flip();
                    byte[]bytes=new byte[buffer.remaining()];
                    buffer.get(bytes);
                    content+=new String(bytes,"UTF-8");
                }
                System.out.println("server echo : "+ content);
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void doConnect() {
        System.out.println("start to connect ...");
        try {
            socketChannel=SocketChannel.open(new InetSocketAddress(host,port));
            System.out.println("connect ok!");
            socketChannel.configureBlocking(false);
            socketChannel.register(selector,SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
