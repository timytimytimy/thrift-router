import com.bosszhipin.bdc.router.zk.ZKConnection;
import com.bosszhipin.bdc.router.zk.ZKRouterConnection;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class RouterTest {

    public static void main(String[] args) throws Exception {
        ZKConnection zkManager = new ZKRouterConnection("192.168.254.103:2181", 2000, "/bdc_router");
        zkManager.connect();
        Thread.sleep(Long.MAX_VALUE);
    }
}
