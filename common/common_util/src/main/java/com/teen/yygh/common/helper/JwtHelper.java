package com.teen.yygh.common.helper;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @author teen
 * @create 2022/3/28 22:27
 */
public class JwtHelper {
    //token过期时间  24小时
    //2022.04.08更新，改为了365天
    private static long tokenExpiration = 24*60*60*1000*365;
//    private static long tokenExpiration = 1;
    private static String tokenSignKey = "123456";  //签名私钥

    /**
     * 根据用户id、用户姓名生成token
     * @param userId
     * @param userName
     * @return
     */
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("YYGH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))  //过期时间为当前时间+24小时
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 从token中获取用户id
     * @param token
     * @return
     */
    public static Long getUserId(String token) {
        //如果token为空  返回空
        if(StringUtils.isEmpty(token)) return null;
        // 通过私钥解析，得到jws对象
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        //获取token中的负荷
        Claims claims = claimsJws.getBody();
        //从负荷中获取userId
        Integer userId = (Integer)claims.get("userId");
        //返回userId
        return userId.longValue();
    }

    /**
     * 从token中获取用户姓名 userName
     * @param token
     * @return
     */
    public static String getUserName(String token) {
        //如果token为空，返回空串
        if(StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        //返回用户姓名
        return (String)claims.get("userName");
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(1L, "55");
        System.out.println(token);
        System.out.println("两个token是否相等："+token.equals("eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDOxMDOyMDYy11EqLU4t8kwBikGYfom5qUAtpqZKtQAL31eIPwAAAA.7PuA62jZ6VG3WpYRxrlcKN_xzR2pSkk7Y7HLVQuGVVQPcsnFSnEyaC53uk_gvb7SxYdMqkcGq_hzX8VET-Yj-w"));
        System.out.println(JwtHelper.getUserName("eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDOxMDOyMDSz0FEqLU4t8kxRsrKEMP0Sc1OBWgwtzI3NjSyMTY0slGoBz99lrUgAAAA.SqmcXj7N_99Bo_UisCas2-P2rKpn8ow0Y1uW0u116C0qdcaYX13oKamYkuwnnlAs1_uBkaOcRThQT5THy0oqlA"));
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUserName(token));
    }
}
