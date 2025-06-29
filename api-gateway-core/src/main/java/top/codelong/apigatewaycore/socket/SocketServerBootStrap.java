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

/**
 * Netty Socket服务端启动类
 * 负责初始化并启动Netty服务器
 */
@Slf4j
@Component
public class SocketServerBootStrap {
    @Resource
    private ServerHandlerInitializer serverHandlerInitializer;
    @Resource
    private GlobalConfiguration config;

    private EventLoopGroup boss;
    private EventLoopGroup work;

    /**
     * 初始化事件循环组
     * @param bossThreads 主线程数
     * @param workThreads 工作线程数
     */
    private void initEventLoopGroup(Integer bossThreads, Integer workThreads) {
        log.info("初始化Netty事件循环组，boss线程数: {}, work线程数: {}", bossThreads, workThreads);
        this.boss = new NioEventLoopGroup(bossThreads);
        this.work = new NioEventLoopGroup(workThreads);
    }

    /**
     * 初始化方法，在Spring容器启动后自动调用
     */
    @PostConstruct
    public void init() {
        log.info("开始启动Netty服务器...");
        Channel channel = this.start(config.getNettyPort(), config.getBossThreads(), config.getWorkerThreads());
        if (channel == null) {
            log.error("Netty服务器启动失败");
            throw new RuntimeException("服务启动失败");
        }
        log.info("Netty服务器启动成功，监听端口: {}", config.getNettyPort());
    }

    /**
     * 启动Netty服务器
     * @param nettyPort 监听端口
     * @param bossThreads 主线程数
     * @param workThreads 工作线程数
     * @return 绑定的Channel对象
     */
    public Channel start(Integer nettyPort, Integer bossThreads, Integer workThreads) {
        log.debug("准备启动Netty服务器，端口: {}, boss线程: {}, work线程: {}",
                nettyPort, bossThreads, workThreads);

        // 初始化线程组
        initEventLoopGroup(bossThreads, workThreads);

        try {
            log.debug("配置ServerBootstrap参数...");
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)  // 连接队列大小
                    .childHandler(serverHandlerInitializer);  // 设置处理器初始化器

            log.info("开始绑定端口: {}...", nettyPort);
            Channel channel = b.bind(nettyPort).sync().channel();
            log.info("端口绑定成功: {}", nettyPort);
            return channel;
        } catch (Exception e) {
            log.error("网关服务启动失败，端口: {}", nettyPort, e);
            // 关闭线程组
            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (work != null) {
                work.shutdownGracefully();
            }
        }
        return null;
    }
}