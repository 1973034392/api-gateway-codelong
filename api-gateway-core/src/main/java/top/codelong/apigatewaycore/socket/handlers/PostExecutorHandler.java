package top.codelong.apigatewaycore.socket.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.socket.BaseHandler;
import top.codelong.apigatewaycore.socket.custom.post.CustomPostHandler;
import top.codelong.apigatewaycore.utils.RequestResultUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 后置执行处理器
 * 负责执行所有自定义的后置处理器链
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class PostExecutorHandler extends BaseHandler<FullHttpRequest> {
    // 自定义后置处理器列表（按order排序）
    private final List<CustomPostHandler> handlers;

    /**
     * 构造函数
     * @param customPostHandlers 自定义后置处理器列表（Spring自动注入）
     */
    @Autowired
    public PostExecutorHandler(List<CustomPostHandler> customPostHandlers) {
        // 根据order排序处理器
        this.handlers = customPostHandlers.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
        log.info("初始化PostExecutorHandler，加载{}个后置处理器", handlers.size());
    }

    /**
     * 处理HTTP请求的后置处理
     * @param ctx ChannelHandler上下文
     * @param channel 当前Channel
     * @param request HTTP请求
     */
    @Override
    protected void handle(ChannelHandlerContext ctx, Channel channel, FullHttpRequest request) {
        log.debug("开始执行后置处理链");

        // 从Channel属性获取HttpStatement
        HttpStatement httpStatement = (HttpStatement) channel.attr(AttributeKey.valueOf("HttpStatement")).get();
        log.trace("获取到HttpStatement: {}", httpStatement);

        // 按顺序执行所有后置处理器
        for (CustomPostHandler handler : handlers) {
            String handlerName = handler.getClass().getSimpleName();
            log.debug("执行后置处理器: {}", handlerName);

            Result result = null;
            try {
                result = handler.handle(httpStatement, request);
                log.trace("处理器[{}]返回结果: {}", handlerName, result);
            } catch (Exception e) {
                log.error("后置处理器[{}]执行异常", handlerName, e);
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

        log.debug("所有后置处理器执行完毕，继续处理流程");
        ctx.fireChannelRead(request);
    }
}