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
    private int count = 0;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean connected;

    EchoClientHandler(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.exit(1);
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            doConnect(count);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
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
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (selectionKey.isReadable()) {
                System.out.println("receive readable!");
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                String content = "";
                while (socketChannel.read(buffer) > 0) {
                    socketChannel.read(buffer);
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    content += new String(bytes, "UTF-8");
                }
                if (socketChannel.read(buffer) < 0) {
                    selectionKey.cancel();
                    socketChannel.close();
                    System.out.println("server error exit!");
                    doConnect(++count);
                    return;
                }
                System.out.println("server echo : " + content);
                selectionKey.interestOps(SelectionKey.OP_READ);
            } else if (selectionKey.isConnectable()) {
                System.out.println("receive connectable!");
                try {
                    if (socketChannel.finishConnect()) {
                        System.out.println("connect success!");
                        connected = true;
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        new Thread(new OutputHandler()).start();
                    } else {
                        System.out.println("connect fail!");
                        doConnect(count);
                    }
                } catch (Exception e) {
                    doConnect(++count);
                }

            }
        }
    }

    private void doConnect(int count) {
        if (count == 0) {
            System.out.println("start to connect ...");
        } else if (count < 5) {
            System.out.println("restart to connect ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("connect fail exit ...");
            System.exit(1);
        }

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            if (socketChannel.connect(new InetSocketAddress(host, port))) {
                System.out.println("connect ok!");
                connected = true;
                socketChannel.register(selector, SelectionKey.OP_READ);
                new Thread(new OutputHandler()).start();
            } else {
                System.out.println("register connect listen!");
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class OutputHandler implements Runnable {



        @Override
        public void run() {
            try {
                doWrite();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void doWrite() throws IOException {
            Scanner scanner = new Scanner(System.in);
            while (connected) {
                System.out.println("Please input what you want: ");
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    socketChannel.write(ByteBuffer.wrap(line.getBytes()));
                }
            }
        }
    }


}
