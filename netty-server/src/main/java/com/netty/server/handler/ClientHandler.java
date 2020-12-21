package com.netty.server.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netty.server.constants.CommonConstants;
import com.netty.server.protocol.MessageCode;
import com.netty.server.utils.NettyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName ClientHandler
 * @Description channel数据处理
 * @Author zhanguowei
 * @Date 2020/12/15 10:54
 * @Version 1.0
 */
public class ClientHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private WebSocketServerHandshaker shaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (msg instanceof FullHttpRequest) {
//            handHttpRequest(ctx, (FullHttpRequest) msg);
//        } else if (msg instanceof WebSocketFrame) {
//            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
//        }
        if (msg instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 判断Channel是否读空闲, 读空闲时移除Channel
            if (event.state().equals(IdleState.READER_IDLE)) {
                final String remoteAddress = NettyUtil.parseChannelRemoteAddr(ctx.channel());
                logger.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
                UserManager.removeChannel(ctx.channel());
                UserManager.broadCastInfo(MessageCode.SYS_USER_COUNT, UserManager.getAuthUserCount());
            }
        }

        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //保存通道
            UserManager.addChannel(ctx.channel());
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            logger.warn("the protocol don't websocket ");
            ctx.channel().close();
            return;
        }

        //websocket 握手返回
        WebSocketServerHandshakerFactory webSocketServerHandshaker = new WebSocketServerHandshakerFactory(
                CommonConstants.WEBSOCKET_URL_PREFIX + request.headers().get(HttpHeaderNames.HOST) + request.uri(), null, true);
        shaker = webSocketServerHandshaker.newHandshaker(request);
        if (shaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 回应握手请求
            shaker.handshake(ctx.channel(), request);
            //保存通道
            UserManager.addChannel(ctx.channel());
        }
    }

    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //是否为链路关闭消息
        if (frame instanceof CloseWebSocketFrame) {
            shaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            //移除用户
            UserManager.removeChannel(ctx.channel());
            return;
        }

        //是否PING消息
        if (frame instanceof PingWebSocketFrame) {
            logger.info("ping message : {}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //是否为PONG
        if (frame instanceof PongWebSocketFrame) {
            logger.info("pong message : {}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //是否为文本消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedMessageTypeException(frame.getClass().getName() + "frame is not supported");
        }

        String message = ((TextWebSocketFrame) frame).text();
        JSONObject jsonObject = JSON.parseObject(message);
        int code = jsonObject.getInteger("code");
        Channel channel = ctx.channel();
        switch (code) {
            case MessageCode.PING_CODE:
            case MessageCode.PONG_CODE:
                //更新用户最近在线时间
                UserManager.updateUserTime(ctx.channel());
                logger.info("receive pong message, address: {}", NettyUtil.parseChannelRemoteAddr(channel));
                return;
            case MessageCode.AUTH_CODE:
                boolean isSuccess = UserManager.saveUser(channel, jsonObject.getString("nick"));
                UserManager.sendInfo(channel, MessageCode.SYS_AUTH_STATE, isSuccess);
                if (isSuccess) {
                    //广播用户上线消息
                    UserManager.broadCastInfo(MessageCode.SYS_USER_COUNT, UserManager.getAuthUserCount());
                }
                return;
            case MessageCode.MESS_CODE:
                //普通的消息留给MessageHandler处理
                break;
            default:
                logger.warn("the code [{}] can't be auth!!!", code);
                return;
        }
        //后续消息交给MessageHandler处理
        ctx.fireChannelRead(frame.retain());
    }
}
