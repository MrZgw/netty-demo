package com.netty.server.entity;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * @ClassName UserInfo
 * @Description 用户信息
 * @Author zhanguowei
 * @Date 2020/12/20 16:27
 * @Version 1.0
 */
@Data
public class UserInfo {

    private boolean isAuth = false; // 是否认证

    private long time = 0;  // 登录时间

    private int userId;     // UID

    private String nick;    // 昵称

    private String addr;    // 地址

    private Channel channel;// 通道
}
