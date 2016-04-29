package com.bosszhipin.bdc.thrift.server;

import com.bosszhipin.bdc.router.HostPort;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * Created by timytimytimy on 2016/4/28.
 */
public class ZKRegister {
    private static Logger logger = LoggerFactory.getLogger(ZKRegister.class);
    private Class<?> clazz;

    private String zkhosts;
    private int zksession;
    private String rootPath;
    private ZooKeeper zooKeeper;
    private int port;

    public ZKRegister(Class<?> clazz, int port) {
        this.clazz = clazz;
        this.port = port;
    }

    private String[] getMethods() throws Exception {
        this.clazz.newInstance();
        String ifaceClazz = clazz.getName() + "$Iface";
        Class<?> ifaceClass = Class.forName(ifaceClazz);
        Method[] methods = ifaceClass.getMethods();
        String[] ms = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            ms[i] = methods[i].getName();
        }
        return ms;
    }

    private HostPort getLocalHostPort() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress().toString();
        return new HostPort(host, this.port);
    }

    private void init(String hosts, int session, String root) {
        zkhosts = hosts;
        zksession = session;
        rootPath = root;
    }

    private void registerZK() throws Exception {
        HostPort hp = getLocalHostPort();
        zooKeeper = new ZooKeeper(zkhosts, zksession, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {

            }
        });
        for (String service : getMethods()) {
            String path = rootPath + "/" + service;
            if (zooKeeper.exists(path, false) != null) {
                byte[] data = zooKeeper.getData(path, false, new Stat());
                String servers = new String(data);
                if (!servers.contains(hp.toString())) {
                    servers += ("," + hp.toString());
                    zooKeeper.setData(path, servers.getBytes(), -1);
                    logger.info("[ZooKeeper] Add server address {} to {}", hp.toString(), path);
                } else {
                    logger.info("[ZooKeeper] service {}={} exists!", path, hp.toString());
                }
            } else {
                zooKeeper.create(path, hp.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info("[ZooKeeper] Register new service {} address {}", path, hp.toString());
            }
        }
    }

    public void registerZK(String hosts, int session, String root) throws Exception {
        init(hosts, session, root);
        registerZK();
    }
}
