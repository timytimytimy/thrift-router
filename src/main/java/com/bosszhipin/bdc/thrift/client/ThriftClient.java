package com.bosszhipin.bdc.thrift.client;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import java.lang.reflect.Constructor;

/**
 * Created by timytimytimy on 2016/4/26.
 */
public class ThriftClient<T> {

    public synchronized T getClinet(Class<T> clientType, String host, int port) {
        try {
            TSocket socket = new TSocket(host, port);
            TFramedTransport transport = new TFramedTransport(socket);
            TCompactProtocol protocol = new TCompactProtocol(transport);
            transport.open();
            Constructor con = clientType.getDeclaredConstructor(new Class[]{TProtocol.class}); //用Object.class代替T
            T client = (T) con.newInstance(new Object[]{protocol});
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized T getClinet(Class<T> clientType, String host, int port, int timeout) {
        try {
            TSocket socket = new TSocket(host, port);
            socket.setSocketTimeout(timeout);
            TFramedTransport transport = new TFramedTransport(socket);
            TCompactProtocol protocol = new TCompactProtocol(transport);
            transport.open();
            Constructor con = clientType.getDeclaredConstructor(new Class[]{TProtocol.class}); //用Object.class代替T
            T client = (T) con.newInstance(new Object[]{protocol});
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}