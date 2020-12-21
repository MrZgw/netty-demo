package com.netty.server.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netty.server.entity.UserInfo;
import com.netty.server.protocol.MessageCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName MessageHandler
 * @Description 消息处理器
 * @Author zhanguowei
 * @Date 2020/12/20 16:24
 * @Version 1.0
 */
public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        UserInfo userInfo = UserManager.getUserInfo(ctx.channel());
        if (userInfo != null) {
            //广播消息到所有用户
            JSONObject json = JSON.parseObject(frame.text());
            UserManager.broadcastMessage(userInfo.getUserId(), userInfo.getNick(), json.getString("mess"));
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        UserManager.removeChannel(ctx.channel());
        UserManager.broadCastInfo(MessageCode.SYS_USER_COUNT, UserManager.getAuthUserCount());
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection error and close the channel", cause);
        UserManager.removeChannel(ctx.channel());
        UserManager.broadCastInfo(MessageCode.SYS_USER_COUNT, UserManager.getAuthUserCount());
    }
}
