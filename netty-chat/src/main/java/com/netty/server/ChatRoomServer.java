package com.netty.server;

import com.netty.server.core.AbstractBaseServer;
import com.netty.server.handler.ClientHandler;
import com.netty.server.handler.MessageHandler;
import com.netty.server.handler.UserManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ChatRoomServer
 * @Description 服务实现类
 * @Author zhanguowei
 * @Date 2020/12/15 10:37
 * @Version 1.0
 */
public class ChatRoomServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomServer.class);

    private ScheduledExecutorService executorService;

    public static final int DEFAULT_PORT = 9688;

    public ChatRoomServer() {
        this(DEFAULT_PORT);
    }

    public ChatRoomServer(int port) {
        this.port = port;
        this.executorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void start() {
        try {
            sb.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    //请求解码器
                                    new HttpServerCodec(),
                                    //将多个消息转换成单一的消息对象
                                    new HttpObjectAggregator(65536),
                                    //支持异步发送大的码流，一般用于发送文件流
                                    new ChunkedWriteHandler(),
                                    //websocket握手处理
                                    new WebSocketServerProtocolHandler("/websocket", null, true, 10485760),
                                    //心跳事件处理
                                    new IdleStateHandler(60, 0, 0),
                                    //websocket连接处理
                                    new ClientHandler(),
                                    //消息处理
                                    new MessageHandler());
                        }
                    });
            cf = sb.bind(port).sync();
            logger.info("ws start success,port is : {}", port);

            //定时发送心跳
            executorService.scheduleAtFixedRate(UserManager::broadCastPing, 3, 35, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("ws start fail:{}", e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        super.shutdown();
    }
}
