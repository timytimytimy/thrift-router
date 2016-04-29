package com.bosszhipin.bdc.router.zk;

import java.io.IOException;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public interface ZKConnection {
    void connect() throws IOException, InterruptedException;
}
