package com.bosszhipin.bdc.router.zk;

import com.bosszhipin.bdc.router.HostPort;
import com.bosszhipin.bdc.router.Scheduler;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class ZKRouterConnection implements ZKConnection {
    private static Logger logger = LoggerFactory.getLogger(ZKRouterConnection.class);

    protected ZooKeeper zooKeeper;
    protected String hosts;
    private int session;
    private String rootPath;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public ZKRouterConnection(String hosts, int session, String rootPath) {
        this.hosts = hosts;
        this.session = session;
        this.rootPath = rootPath;
    }

    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(hosts, session, new RouterWatcher());
        initReconnect();
        connectedSignal.await();
    }

    private void initReconnect() {
        Timer timer = new Timer();
        timer.schedule(new Reconnecter(), 1, session - 3);
        Timer timer1 = new Timer();
        timer1.schedule(new Ping(), 1, 10 * 1000);
    }

    class Reconnecter extends TimerTask {
        public void run() {
            try {
                zooKeeper.getChildren(rootPath, true);
                logger.debug("zookeeper reconnect success");
            } catch (Exception e) {
                logger.error("zookeeper reconnect error", e);
            }
        }
    }

    class Ping extends TimerTask {
        private String getServiceName(String path) {
            if (path.startsWith("/")) {
                String[] items = path.split("/");
                if (items.length >= 3) {
                    return items[2];
                }
            }
            return null;
        }

        private void removeAddr(String path, HostPort hp) throws KeeperException, InterruptedException {
            byte[] data = zooKeeper.getData(path, true, new Stat());
            String servers = new String(data);
            servers += ",";
            servers = servers.replace(hp.toString(), "");
            if (servers.endsWith(",")) {
                servers = servers.substring(0, servers.length() - 1);
            }
            servers = servers.trim();
            if (servers.equals("")) {
                zooKeeper.delete(path, -1);
            } else {
                zooKeeper.setData(path, servers.getBytes(), -1);
            }
            Scheduler.INSTANCE.removeNode(getServiceName(path), hp.toString());
            logger.warn("{} {} dead, remove it", path, hp);
        }

        private void ping(String path, HostPort hp) throws KeeperException, InterruptedException {
            Socket client = null;
            try{
                client = new Socket(hp.getHost(), hp.getPort());
                client.close();
            }catch(Exception e){
                removeAddr(path, hp);
            }
        }

        @Override
        public void run() {
            try {
                List<String> nodes = zooKeeper.getChildren(rootPath, true);
                for (String node : nodes) {
                    String path = rootPath + "/" + node;
                    byte[] data = zooKeeper.getData(path, true, new Stat());
                    String[] servers = new String(data).split(",");
                    for (String addr : servers) {
                        ping(path, new HostPort(addr));
                    }
                }
            } catch (Exception e) {
                logger.error("zookeeper ping error", e);
            }
        }
    }

    class RouterWatcher implements Watcher {
        private boolean FIRST = true;

        public void process(WatchedEvent event) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                switch (event.getType()) {
                    case NodeDataChanged:
                    case NodeCreated:
                    case NodeChildrenChanged:
                    case NodeDeleted:
                        initServerList();
                }
                if (FIRST) {
                    initServerList();
                    connectedSignal.countDown();
                    FIRST = false;
                }
            }
        }

        private String[] getServerList(String path) throws KeeperException, InterruptedException {
            byte[] data = zooKeeper.getData(path, true, new Stat());
            String ds = new String(data);
            return ds.split(",");
        }

        //获取及更新全部服务及地址
        private void initServerList() {
            try {
                List<String> nodes = zooKeeper.getChildren(rootPath, true);
                ConcurrentHashMap<String, String[]> serviceMaps = new ConcurrentHashMap<String, String[]>();
                for (String servicNode : nodes) {
                    String path = rootPath + "/" + servicNode;
                    String[] serverList = getServerList(path);
                    serviceMaps.put(servicNode, serverList);
                    logger.info("[ Service Add ] {}={}", servicNode, serverList);
                }
                Scheduler.INSTANCE.setNodeMap(serviceMaps);
            } catch (Exception e) {
                logger.error("error", e);
            }
        }

        //更新节点
        private void updateServerList(String path) {
            try {
                zooKeeper.getChildren(rootPath, true);
                String serviceName = getServiceName(path);
                if (serviceName != null) {
                    String[] serverList = getServerList(path);
                    Scheduler.INSTANCE.getNodeMap().put(serviceName, serverList);
                    logger.info("[Service Update] {}={}", serviceName, serverList);
                }
            } catch (Exception e) {
                logger.error("error", e);
            }
        }

        //删除节点
        private void deleteService(String path) {
            try {
                zooKeeper.getChildren(rootPath, true);
                String serviceName = getServiceName(path);
                if (serviceName != null) {
                    Scheduler.INSTANCE.getNodeMap().remove(serviceName);
                    Scheduler.INSTANCE.getRoundMap().remove(serviceName);
                    logger.info("[Service Delete] {}", serviceName);
                }
            } catch (Exception e) {
                logger.error("error", e);
            }
        }

        private String getServiceName(String path) {
            if (path.startsWith("/")) {
                String[] items = path.split("/");
                if (items.length >= 3) {
                    return items[2];
                }
            }
            return null;
        }
    }
}
