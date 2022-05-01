package com.atguigu.srb.base.utils;

import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.exception.BusinessException;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private final static long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000;
    private final static String TOKEN_SIGN_KEY = "c1o2d3e4r5x6d7h8";

    /**
     * 生成 Key 类型的密钥
     *
     * @return 密钥
     */
    private static Key getKeyInstance() {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] bytes = DatatypeConverter.parseBase64Binary(TOKEN_SIGN_KEY);
        return new SecretKeySpec(bytes, signatureAlgorithm.getJcaName());
    }

    /**
     * 生成 token
     *
     * @param userId   用户ID
     * @param userName 用户名
     * @return token 字符串
     */
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("SRB-USER")
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, getKeyInstance())
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 判断 token 是否有效
     *
     * @param token token
     * @return 有效：true，无效：false
     */
    public static boolean checkToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 通过 token 获取用户ID
     *
     * @param token token
     * @return userId
     */
    public static Long getUserId(String token) {
        Claims claims = getClaims(token);
        Integer userId = (Integer) claims.get("userId");
        return userId.longValue();
    }

    /**
     * 通过 token 获取用户名
     *
     * @param token token
     * @return 用户名
     */
    public static String getUserName(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("userName");
    }

    /**
     * 移除 token
     *
     * @param token token
     */
    public static void removeToken(String token) {
        // jwttoken无需删除，客户端扔掉即可。
    }

    /**
     * 校验 token 并返回 Claims
     *
     * @param token token
     * @return 该 token 的 Claims 对象
     */
    private static Claims getClaims(String token) {
        if (StringUtils.isEmpty(token)) {
            // LOGIN_AUTH_ERROR(-211, "未登录"),
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return claims;
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }
}

