package top.codelong.apigatewaycore.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketServerBootStrap {
    private EventLoopGroup boss;
    private EventLoopGroup work;

    private void initEventLoopGroup(Integer bossThreads, Integer workThreads) {
        this.boss = new NioEventLoopGroup(bossThreads);
        this.work = new NioEventLoopGroup(workThreads);
    }

    public Channel start(Integer nettyPort, Integer bossThreads, Integer workThreads) {
        initEventLoopGroup(bossThreads, workThreads);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ServerHandlerInitializer());

            return b
                    .bind(nettyPort).sync()
                    .channel();
        } catch (Exception e) {
            log.error("网关服务启动失败", e);
        }
        return null;
    }
}
