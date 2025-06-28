package top.codelong.apigatewaycore.connection;

public interface BaseConnection {
    Object send(Object parameter);

    void close();
}
