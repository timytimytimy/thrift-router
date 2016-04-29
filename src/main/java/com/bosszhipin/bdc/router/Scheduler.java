package com.bosszhipin.bdc.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by timytimytimy on 2016/4/27.
 * 调度程序，决定转发到那个server上
 */
public class Scheduler {
    private static Logger logger = LoggerFactory.getLogger(Scheduler.class);
    public static Scheduler INSTANCE;

    private ConcurrentHashMap<String, String[]> nodeMap;
    private ConcurrentHashMap<String, Integer> roundMap;

    static {
        INSTANCE = new Scheduler();
    }

    /**
     * 解包thrift提取方法名
     * @param data
     * @return
     */
    private String getThriftMethodName(byte[] data) {
        int len = data[7];
        byte[] nb = new byte[len];
        for (int i = 0; i < len; i++) {
            nb[i] = data[8 + i];
        }
        return new String(nb);
    }

    private synchronized String roundRobin(String method) {
        int i = roundMap.get(method);
        i = (i + 1) % nodeMap.get(method).length;
        roundMap.put(method, i);
        return nodeMap.get(method)[i];
    }

    public HostPort getRemoteUrl(byte[] data) {
        String method = getThriftMethodName(data);
        HostPort hp = new HostPort(roundRobin(method));
        logger.info("[Transport] {} to {}", method, hp);
        return hp;
    }

    public void removeNode(String service, String addr) {
        String[] ips = nodeMap.get(service);
        boolean has = false;
        for (String ip : ips) {
            if (ip.equals(addr)) {
                has = true;
                break;
            }
        }
        String[] newips = new String[ips.length];
        if (has) newips = new String[ips.length -1];
        int i = 0;
        for (String ip : ips) {
            if (!ip.equals(addr)) {
                newips[i] = ip;
                i++;
            }
        }
        nodeMap.put(service, newips);
        logger.warn("[Remove] {} from {}", addr, service);
    }

    public Map<String, String[]> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(ConcurrentHashMap<String, String[]> nodeMap) {
        this.nodeMap = nodeMap;
        roundMap = new ConcurrentHashMap<String, Integer>();
        for (String key: nodeMap.keySet()) {
            roundMap.put(key, 0);
        }
    }

    public ConcurrentHashMap<String, Integer> getRoundMap() {
        return roundMap;
    }
}