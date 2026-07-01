package com.ttbt.smartclass.aop;

import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.enums.UserRoleEnum;
import com.ttbt.smartclass.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    @Lazy
    private UserService userService;

    /**
     * 拦截带 AuthCheck 注解的方法并校验当前用户角色。
     *
     * @param joinPoint 被拦截的方法调用
     * @param authCheck 权限注解配置
     * @return 原方法执行结果
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 从注解读取接口要求的角色，并从当前请求中获取登录用户
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);

        // 将用户角色字符串转换为枚举，未知角色直接判定无权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 封禁用户不能访问任何需要权限校验的接口
        if (UserRoleEnum.BAN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 管理员全局放行（业务层再校验数据归属）
        if (UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            return joinPoint.proceed();
        }
        if (mustRole == null || mustRole.trim().isEmpty()) {
            return joinPoint.proceed();
        }
        mustRole = mustRole.trim();

        // 支持逗号分隔：任一角色满足即可（在管理员已放行后，此处仅非管理员）
        if (mustRole.contains(",")) {
            boolean ok = false;
            for (String part : mustRole.split(",")) {
                UserRoleEnum need = UserRoleEnum.getEnumByValue(part.trim());
                if (need != null && roleMatchesNonAdmin(userRoleEnum, need)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            return joinPoint.proceed();
        }

        // 单角色校验：非管理员不能访问仅管理员接口，教师接口只允许教师访问
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (UserRoleEnum.TEACHER.equals(mustRoleEnum)) {
            if (!UserRoleEnum.TEACHER.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 非管理员用户是否满足所需单一角色
     */
    private static boolean roleMatchesNonAdmin(UserRoleEnum user, UserRoleEnum required) {
        if (UserRoleEnum.TEACHER.equals(required)) {
            return UserRoleEnum.TEACHER.equals(user);
        }
        if (UserRoleEnum.USER.equals(required)) {
            return UserRoleEnum.USER.equals(user);
        }
        return false;
    }
}
