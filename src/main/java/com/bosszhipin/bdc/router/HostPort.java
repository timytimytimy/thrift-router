package com.bosszhipin.bdc.router;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class HostPort {
    private String host;
    private int port;

    public HostPort(String url) {
        String[] items = url.split(":");
        host = items[0];
        port = Integer.valueOf(items[1]);
    }

    public HostPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HostPort(String host, String port) {
        this.host = host;
        this.port = Integer.valueOf(port);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
