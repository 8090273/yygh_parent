package com.teen.yygh.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.common.helper.JwtHelper;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.common.result.ResultCodeEnum;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author teen
 * @create 2022/3/31 9:50
 */
@Component
public class AuthGlobalFilter implements GlobalFilter , Ordered {

    //应该是路径匹配器
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //得到request对象
        ServerHttpRequest request = exchange.getRequest();
        //获得接口路径
        String path = request.getURI().getPath();
        System.out.println("==="+path);

        //内部服务接口，不允许外部访问
        if(antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //api接口，异步请求，校验用户必须登录
        if(antPathMatcher.match("/api/**/auth/**", path)) {
            //从toke中获得用户userId，没有toke肯定获取不到
            Long userId = this.getUserId(request);
            //如果userId为空，说明未登录，返回鉴权失败
            if(StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * api接口鉴权失败返回数据
     * @param response
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 获取当前登录用户id
     * @param request
     * @return
     */
    private Long getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(null  != tokenList) {
            token = tokenList.get(0);
        }
        if(!StringUtils.isEmpty(token)) {
            return JwtHelper.getUserId(token);
        }
        return null;
    }
}

