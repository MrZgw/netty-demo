package com.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName ChatRoomMain
 * @Description 启动类
 * @Author zhanguowei
 * @Date 2020/12/19 22:17
 * @Version 1.0
 */
public class ChatRoomMain {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomMain.class);

    public static void main(String[] args) {
        int port = ChatRoomServer.DEFAULT_PORT;
        if (args != null && args.length > 1) {
            port = Integer.valueOf(args[0]);
        }
        final ChatRoomServer server = new ChatRoomServer(port);
        server.init();
        server.start();
        // 注册进程钩子，在JVM进程关闭前释放资源
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
                logger.warn(">>>>>> jvm shutdown <<<<<<");
                System.exit(0);
            }
        });
    }
}
