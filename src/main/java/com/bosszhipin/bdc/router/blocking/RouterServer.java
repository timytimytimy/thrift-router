package com.bosszhipin.bdc.router.blocking;

import com.bosszhipin.bdc.router.zk.ZKConnection;
import com.bosszhipin.bdc.router.zk.ZKRouterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class RouterServer {
    private static Logger logger = LoggerFactory.getLogger(RouterServer.class);
    private static final int NTHREADS = 100;
    private static final Executor exec = Executors.newCachedThreadPool();

    int localPort = 9909;

    public RouterServer(int port) {
        this.localPort = port;
    }

    public void init(String zkHosts, int zkSession, String zkRoot) throws Exception {
        ZKConnection zkManager = new ZKRouterConnection(zkHosts, zkSession, zkRoot);
        zkManager.connect();
    }

    public void serve() throws IOException {
        ServerSocket serverSocket = new ServerSocket(localPort);
        logger.info("启动本地监听端口 {} 成功！", localPort);

        while (true) {
            Socket clientSocket = null;
            try {
                //获取客户端连接
                clientSocket = serverSocket.accept();
                logger.info("Accept one client {}", clientSocket.getRemoteSocketAddress());
                //启动数据转换接口
                exec.execute(new TransportSender(clientSocket));

            } catch (Exception ex) {
                logger.error("error", ex);
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }
        }
    }

    public static void main(String[] args) {
        RouterServer server = new RouterServer(9909);
        try {
//            server.init("192.168.254.103:2181", 10000, "/bdc_router");
            server.serve();
        } catch (Exception e) {
            logger.error("启动失败", e);
        }
    }

}
