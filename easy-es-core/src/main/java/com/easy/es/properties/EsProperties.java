package com.easy.es.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Deprecated
@Data
@ConfigurationProperties(prefix = "frame.es")
public class EsProperties {

    /**
     * 集群地址，多个用,隔开
     */

    private String  nodes;
    /**
     * 使用的协议
     */

    private String schema;

    /**
     * 用户名称
     */

    private String userName;

    /**
     * 密码
     */

    private String password;

    /**
     * 连接超时时间
     */

    private int connectTimeOut;
    /**
     * 连接超时时间
     */

    private int socketTimeOut;
    /**
     * 获取连接的超时时间
     */

    private int connectionRequestTimeOut;
    /**
     * 最大连接数
     */

    private int maxConnectNum;
    /**
     * 最大路由连接数
     */

    private int maxConnectPerRoute;
}
