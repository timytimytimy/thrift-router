package test;

import com.bosszhipin.thrift.TestReq;
import com.bosszhipin.thrift.TestRes;
import com.bosszhipin.thrift.TestService;
import org.apache.thrift.TException;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class TestServiceImpl implements TestService.Iface {
    @Override
    public TestRes testMe(TestReq req) throws TException {
        TestRes res = new TestRes();
        res.setParam1(req.getParam1() + 1);
        res.setParam2(req.getParam2() + "bbb");
        return res;
    }

    @Override
    public void ping() throws TException {
        System.out.println("ping");
    }
}
