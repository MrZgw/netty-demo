package com.netty.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * @ClassName BineraMessageHandler
 * @Description 文件消息处理器
 * @Author zhanguowei
 * @Date 2020/12/21 14:46
 * @Version 1.0
 */
public class BinaryMessageHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame binaryWebSocketFrame) throws Exception {

    }
}
