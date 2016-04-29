package com.bosszhipin.bdc.thrift.server;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Created by timytimytimy on 2016/4/27.
 */
public class ThriftServer {
    private static Logger logger = LoggerFactory.getLogger(ThriftServer.class);

    private static int numProcessors = Runtime.getRuntime().availableProcessors();
    private static int numSelectorThreads = numProcessors;
    private static int numWorkerThreads = 2 * numProcessors;
    private static int acceptQueueSizePerThread = 64;
    private static int stopTimeoutSeconds = 5;

    private TThreadedSelectorServer server;
    private String zkhosts;
    private int zksession;
    private String rootPath;
    private Class<?> clazz;
    private Object impl;
    private int port;

    public ThriftServer(Class<?> clazz, Object impl, int port) throws Exception {
        this.port = port;
        this.clazz = clazz;
        this.impl = impl;
    }

    public void init(String hosts, int session, String root) {
        zkhosts = hosts;
        zksession = session;
        rootPath = root;
    }

    public void serve() throws Exception {
        initServer();
        if (StringUtils.isEmpty(zkhosts)) {
            logger.warn("Zookeeper cann't be found, if you want to register server to zookeeper automatically, please use method 'init' first.");
        } else {
            registerZK(zkhosts, zksession, rootPath);
        }
        logger.info("Server started at port {}", this.port);
        this.server.serve();
    }

    private void registerZK(String hosts, int session, String root) throws Exception {
        ZKRegister register = new ZKRegister(this.clazz, this.port);
        register.registerZK(hosts, session, root);
    }

    private TProcessor getTProcessor(Class<?> classType) throws Exception {
        String processorClazz = this.clazz.getName() + "$Processor";
        String ifaceClazz = this.clazz.getName() + "$Iface";
        classType.newInstance();
        Class<?> processorClass = Class.forName(processorClazz);
        Class<?> ifaceClass = Class.forName(ifaceClazz);
        Constructor<?> con = processorClass.getDeclaredConstructor(new Class[]{ifaceClass});
        TProcessor processor = (TProcessor) con.newInstance(new Object[]{this.impl});
        return processor;
    }

    private void initServer() throws Exception {
        TProcessor processor = getTProcessor(this.clazz);
        TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(this.port);
        TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverSocket);
        args.processor(processor);
        args.protocolFactory(new TCompactProtocol.Factory());
        args.inputTransportFactory(new TFramedTransport.Factory());
        args.selectorThreads(numSelectorThreads);
        args.workerThreads(numWorkerThreads);
        args.acceptQueueSizePerThread(acceptQueueSizePerThread);
        args.acceptPolicy(TThreadedSelectorServer.Args.AcceptPolicy.FAST_ACCEPT);
        args.stopTimeoutVal(stopTimeoutSeconds);

        this.server = new TThreadedSelectorServer(args);
    }

    public static int getNumProcessors() {
        return numProcessors;
    }

    public static void setNumProcessors(int numProcessors) {
        ThriftServer.numProcessors = numProcessors;
    }

    public static int getNumSelectorThreads() {
        return numSelectorThreads;
    }

    public static void setNumSelectorThreads(int numSelectorThreads) {
        ThriftServer.numSelectorThreads = numSelectorThreads;
    }

    public static int getNumWorkerThreads() {
        return numWorkerThreads;
    }

    public static void setNumWorkerThreads(int numWorkerThreads) {
        ThriftServer.numWorkerThreads = numWorkerThreads;
    }

    public static int getAcceptQueueSizePerThread() {
        return acceptQueueSizePerThread;
    }

    public static void setAcceptQueueSizePerThread(int acceptQueueSizePerThread) {
        ThriftServer.acceptQueueSizePerThread = acceptQueueSizePerThread;
    }

    public static int getStopTimeoutSeconds() {
        return stopTimeoutSeconds;
    }

    public static void setStopTimeoutSeconds(int stopTimeoutSeconds) {
        ThriftServer.stopTimeoutSeconds = stopTimeoutSeconds;
    }
}
