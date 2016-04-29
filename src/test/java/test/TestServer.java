package test;

import com.bosszhipin.bdc.thrift.server.ThriftServer;
import com.bosszhipin.thrift.TestService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class TestServer {
    public static void main(String[] args) throws Exception {
        int port = 9999;
        ThriftServer server = new ThriftServer(TestService.class, new TestServiceImpl(), port);
        server.init("192.168.254.103", 10000, "/bdc_router");
        server.serve();
    }
}
