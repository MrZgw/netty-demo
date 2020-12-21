package com.netty.server.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName AbstartBaseServer
 * @Description 服务基类
 * @Author zhanguowei
 * @Date 2020/12/19 21:55
 * @Version 1.0
 */
public abstract class AbstractBaseServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseServer.class);

//    protected DefaultEventLoopGroup defLoopGroup;

    protected EventLoopGroup bossGroup;

    protected EventLoopGroup workGroup;

    protected ServerBootstrap sb;

    protected ChannelFuture cf;

    protected int port;

    public void init() {
//        defLoopGroup = new DefaultEventLoopGroup(8, new ThreadFactory() {
//            private AtomicInteger index = new AtomicInteger(0);
//
//            @Override
//            public Thread newThread(Runnable r) {
//                return new Thread(r, "DEFAULTEVENTLOOPGROUP_" + index.incrementAndGet());
//            }
//        });
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup();
        sb = new ServerBootstrap();
        logger.info("init loop group success");
    }

    @Override
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        logger.info("shutdown server...");
    }
}
