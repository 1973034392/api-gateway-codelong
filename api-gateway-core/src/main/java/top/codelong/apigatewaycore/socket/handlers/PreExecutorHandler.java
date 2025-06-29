package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.socket.custom.post.CustomPostHandler;
import top.codelong.apigatewaycore.socket.custom.pre.CustomPreHandler;
import top.codelong.apigatewaycore.utils.RequestResultUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前置处理器执行器，负责按顺序执行所有自定义前置处理器
 * 如果任一前置处理器返回结果，则直接响应并终止处理链
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class PreExecutorHandler extends BaseHandler<FullHttpRequest> {
    private final List<CustomPreHandler> handlers;

    /**
     * 构造函数，初始化并排序所有前置处理器
     * @param customPreHandlers 自定义前置处理器列表
     */
    @Autowired
    public PreExecutorHandler(List<CustomPreHandler> customPreHandlers) {
        // 按照order顺序对处理器进行排序
        this.handlers = customPreHandlers.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
        log.debug("初始化PreExecutorHandler，共加载{}个前置处理器", handlers.size());
    }

    /**
     * 处理HTTP请求，按顺序执行所有前置处理器
     * @param ctx Netty上下文
     * @param channel 通信通道
     * @param request HTTP请求
     */
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        log.debug("开始执行前置处理链");

        // 从Channel属性获取HttpStatement
        HttpStatement httpStatement = (HttpStatement) channel.attr(AttributeKey.valueOf("HttpStatement")).get();
        log.trace("获取到HttpStatement: {}", httpStatement);

        // 按顺序执行所有前置处理器
        for (CustomPreHandler handler : handlers) {
            String handlerName = handler.getClass().getSimpleName();
            log.debug("执行前置处理器: {}", handlerName);

            Result result = null;
            try {
                result = handler.handle(httpStatement, request);
                log.trace("处理器[{}]返回结果: {}", handlerName, result);
            } catch (Exception e) {
                log.error("前置处理器[{}]执行异常", handlerName, e);
                // 忽略异常，继续执行下一个处理器
            }

            // 如果处理器返回了结果，则直接响应
            if (result != null) {
                log.debug("处理器[{}]返回有效结果，终止处理链", handlerName);
                DefaultFullHttpResponse response = RequestResultUtil.parse(result);
                channel.writeAndFlush(response);
                return;
            }
        }

        log.debug("所有前置处理器执行完毕，继续处理流程");
        ctx.fireChannelRead(request);
    }
}