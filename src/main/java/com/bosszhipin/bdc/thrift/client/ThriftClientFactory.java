package com.bosszhipin.bdc.thrift.client;

/**
 * Created by timytimytimy on 2016/4/26.
 */
public class ThriftClientFactory {
    static ThriftClient client = null;

    static {
        client = new ThriftClient();
    }

    public static ThriftClient getInstance() {
        return client;
    }
}
