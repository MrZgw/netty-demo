package com.netty.server.core;

/**
 * @ClassName Server
 * @Description 服务端接口
 * @Author zhanguowei
 * @Date 2020/12/19 21:54
 * @Version 1.0
 */
public interface Server {

    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void shutdown();
}
