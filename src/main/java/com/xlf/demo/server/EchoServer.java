package com.xlf.demo.server;

/**
 * author: xiaoliufu
 * date:   2017/1/2.
 * description: NIO server demo
 */
public class EchoServer {
    public static void main(String[] args) {
        int port=8088;
        if (args!=null && args.length>0){
            port=Integer.parseInt(args[0]);
        }
        EchoServerHandler echoServerHandler =new EchoServerHandler(port);
        new Thread(echoServerHandler).start();
    }
}
