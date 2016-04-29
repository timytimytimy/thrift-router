package com.bosszhipin.bdc.router.blocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by timytimytimy on 2016/4/27.
 * 接收server返回的数据发送给客户端
 */
public class TransportReciver extends Thread {
    private static Logger logger = LoggerFactory.getLogger(TransportReciver.class);
    Socket getDataSocket;
    Socket putDataSocket;
    private static final int MAX_BUFFER = 9999;

    public TransportReciver(Socket getDataSocket, Socket putDataSocket){
        this.getDataSocket = getDataSocket;
        this.putDataSocket = putDataSocket;
    }

    public void run(){
        try {
            while(true){

                InputStream in = getDataSocket.getInputStream();
                //读入数据
                byte[] data = new byte[MAX_BUFFER];
                int readlen = in.read(data);

                //如果没有数据，则暂停
                if(readlen <= 0){
                    Thread.sleep(100);
                    continue;
                }

                OutputStream out = putDataSocket.getOutputStream();
                out.write(data, 0, readlen);
                out.flush();
            }
        } catch (SocketException e) {
//            logger.error("error", e);
        } catch (Exception e) {
            logger.error("error", e);
        }
        finally{
            //关闭socket
            try {
                if(putDataSocket != null){
                    putDataSocket.close();
                }
            } catch (Exception exx) {
            }

            try {
                if(getDataSocket != null){
                    getDataSocket.close();
                }
            } catch (Exception exx) {
            }
        }
    }
}
