package com.xlf.demo.client;

/**
 * author: xiaoliufu
 * date:   2017/1/2.
 * description:
 */
public class EchoClient {
    public static void main(String[] args) {
        int port=8088;
        if (args!=null && args.length>0){
            port=Integer.parseInt(args[0]);
        }
        EchoClientHandler echoClientHandler=new EchoClientHandler("127.0.0.1",port);
        new Thread(echoClientHandler).start();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
