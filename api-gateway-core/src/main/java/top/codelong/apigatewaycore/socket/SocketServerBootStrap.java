package top.codelong.apigatewaycore.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.config.GlobalConfiguration;

@Slf4j
@Component
public class SocketServerBootStrap {
    @Resource
    private ServerHandlerInitializer serverHandlerInitializer;
    @Resource
    private GlobalConfiguration config;

    private EventLoopGroup boss;
    private EventLoopGroup work;

    private void initEventLoopGroup(Integer bossThreads, Integer workThreads) {
        this.boss = new NioEventLoopGroup(bossThreads);
        this.work = new NioEventLoopGroup(workThreads);
    }

    @PostConstruct
    public void init() {
        Channel channel = this.start(config.getNettyPort(), config.getBossThreads(), config.getWorkerThreads());
        if (channel == null) {
            throw new RuntimeException("服务启动失败");
        }
    }

    public Channel start(Integer nettyPort, Integer bossThreads, Integer workThreads) {
        initEventLoopGroup(bossThreads, workThreads);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(serverHandlerInitializer);

            return b
                    .bind(nettyPort).sync()
                    .channel();
        } catch (Exception e) {
            log.error("网关服务启动失败", e);
        }
        return null;
    }
}
