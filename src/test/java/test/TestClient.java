package test;

import com.bosszhipin.bdc.thrift.client.ThriftClientFactory;
import com.bosszhipin.thrift.ResumeSearchRequest;
import com.bosszhipin.thrift.ResumeSearchServer;
import com.bosszhipin.thrift.TestReq;
import com.bosszhipin.thrift.TestService;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class TestClient {
    public static void main(String[] args) throws TException {
//        TestService.Client client = (TestService.Client) ThriftClientFactory.getInstance().getClinet(TestService.Client.class, "127.0.0.1", 9909);
//        TestReq req = new TestReq();
//        req.setParam1(1);
//        req.setParam2("aaaaaa");
//        System.out.println(client.testMe(req));

        ResumeSearchServer.Client client1 = (ResumeSearchServer.Client) ThriftClientFactory.getInstance().getClinet(ResumeSearchServer.Client.class, "127.0.0.1", 9909);
        Map<String, String> params = new HashMap<>();
        ResumeSearchRequest req = new ResumeSearchRequest();
        req.setBossId(0);
        req.setJobId(0);
        req.setPage(1);
        req.setPageSize(10);
        req.setParams(params);
        System.out.println(client1.resumeSearch(req));
    }
}
