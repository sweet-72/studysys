package com.ttbt.smartclass.manager;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ttbt.smartclass.config.JwtProperties;
import com.ttbt.smartclass.model.entity.User;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class JwtTokenManager {

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    @Resource
    private JwtProperties jwtProperties;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 为登录用户生成 JWT 令牌。
     *
     * @param user 登录用户
     * @return JWT 令牌
     */
    public String createToken(User user) {
        // 计算签发时间和过期时间，并写入用户 id 与角色信息
        long nowSeconds = System.currentTimeMillis() / 1000;
        long expSeconds = nowSeconds + jwtProperties.getExpireSeconds();
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", user.getId());
        payload.put("role", user.getUserRole());
        payload.put("iat", nowSeconds);
        payload.put("exp", expSeconds);
        return JWTUtil.createToken(payload, getSecretBytes());
    }

    public Long getUserIdByToken(String token) {
        if (!StringUtils.hasText(token) || isBlacklisted(token)) {
            return null;
        }
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (!jwt.setKey(getSecretBytes()).verify()) {
                return null;
            }
            Long exp = toLong(jwt.getPayload("exp"));
            long nowSeconds = System.currentTimeMillis() / 1000;
            if (exp == null || exp <= nowSeconds) {
                return null;
            }
            return toLong(jwt.getPayload("uid"));
        } catch (Exception e) {
            log.debug("parse jwt token failed", e);
            return null;
        }
    }

    /**
     * 从请求头或 Cookie 中解析 JWT 令牌。
     *
     * @param request 当前 HTTP 请求
     * @return 去除前缀后的令牌
     */
    public String resolveToken(HttpServletRequest request) {
        // 请求为空时无法解析登录态
        if (request == null) {
            return null;
        }
        // 优先从配置的 Authorization 头读取，再兼容旧版 token 请求头
        String token = request.getHeader(jwtProperties.getHeader());
        if (!StringUtils.hasText(token)) {
            token = request.getHeader("token");
        }
        // 请求头没有令牌时，继续兼容 Cookie 中的 token
        if (!StringUtils.hasText(token) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie != null && "token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        // 去掉配置的 Bearer 等令牌前缀，返回纯 JWT 字符串
        String prefix = jwtProperties.getTokenPrefix();
        if (StringUtils.hasText(prefix) && token.startsWith(prefix)) {
            token = token.substring(prefix.length());
        }
        return token.trim();
    }

    /**
     * 将指定 JWT 加入 Redis 黑名单。
     *
     * @param token JWT 令牌
     */
    public void invalidateToken(String token) {
        // 未启用 Redis 或令牌为空时不做黑名单处理
        if (!StringUtils.hasText(token) || stringRedisTemplate == null) {
            return;
        }
        // 解析令牌过期时间，只有仍未过期的令牌才需要加入黑名单
        Long exp = getExpireByToken(token);
        if (exp == null) {
            return;
        }
        long nowSeconds = System.currentTimeMillis() / 1000;
        long ttl = exp - nowSeconds;
        if (ttl <= 0) {
            return;
        }
        // 使用 token 的 MD5 作为黑名单 key，并设置与令牌剩余有效期一致的 TTL
        stringRedisTemplate.opsForValue().set(getBlacklistKey(token), "1", ttl, TimeUnit.SECONDS);
    }

    public long getExpireSeconds() {
        return jwtProperties.getExpireSeconds();
    }

    private byte[] getSecretBytes() {
        return jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    }

    private boolean isBlacklisted(String token) {
        if (stringRedisTemplate == null) {
            return false;
        }
        Boolean existed = stringRedisTemplate.hasKey(getBlacklistKey(token));
        return Boolean.TRUE.equals(existed);
    }

    private String getBlacklistKey(String token) {
        return BLACKLIST_KEY_PREFIX + DigestUtil.md5Hex(token);
    }

    private Long getExpireByToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            if (!jwt.setKey(getSecretBytes()).verify()) {
                return null;
            }
            return toLong(jwt.getPayload("exp"));
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
