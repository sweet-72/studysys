package com.ttbt.smartclass.controller;

import static com.ttbt.smartclass.constant.UserConstant.SALT;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.DeleteRequest;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.config.WxOpenConfig;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.model.dto.user.UserAddRequest;
import com.ttbt.smartclass.model.dto.user.UserLoginByPhoneRequest;
import com.ttbt.smartclass.model.dto.user.UserLoginRequest;
import com.ttbt.smartclass.model.dto.user.UserQueryRequest;
import com.ttbt.smartclass.model.dto.user.UserRegisterByPhoneRequest;
import com.ttbt.smartclass.model.dto.user.UserRegisterRequest;
import com.ttbt.smartclass.model.dto.user.UserUpdateMyRequest;
import com.ttbt.smartclass.model.dto.user.UserUpdateRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.LoginUserVO;
import com.ttbt.smartclass.model.vo.UserVO;
import com.ttbt.smartclass.service.UserService;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    // region 认证相关接口

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 手机号注册。
     */
    @PostMapping("/register/phone")
    public BaseResponse<Long> userRegisterByPhone(@RequestBody UserRegisterByPhoneRequest userRegisterByPhoneRequest) {
        if (userRegisterByPhoneRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPhone = userRegisterByPhoneRequest.getUserPhone();
        String userPassword = userRegisterByPhoneRequest.getUserPassword();
        String checkPassword = userRegisterByPhoneRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userPhone, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long result = userService.userRegisterByPhone(userPhone, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 账号密码登录。
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        writeLoginTokenCookie(response, request, loginUserVO);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 手机号密码登录。
     */
    @PostMapping("/login/phone")
    public BaseResponse<LoginUserVO> userLoginByPhone(@RequestBody UserLoginByPhoneRequest userLoginByPhoneRequest,
            HttpServletRequest request, HttpServletResponse response) {
        if (userLoginByPhoneRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPhone = userLoginByPhoneRequest.getUserPhone();
        String userPassword = userLoginByPhoneRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userPhone, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLoginByPhone(userPhone, userPassword, request);
        writeLoginTokenCookie(response, request, loginUserVO);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 微信开放平台登录。
     */
    @GetMapping("/login/wx_open")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("code") String code) {
        WxOAuth2AccessToken accessToken;
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            String mpOpenId = userInfo.getOpenid();
            if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "login failed, system error");
            }
            LoginUserVO loginUserVO = userService.userLoginByMpOpen(userInfo, request);
            writeLoginTokenCookie(response, request, loginUserVO);
            return ResultUtils.success(loginUserVO);
        } catch (Exception e) {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "login failed, system error");
        }
    }

    /**
     * 退出登录，清理服务端会话和客户端 cookie。
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        clearLoginTokenCookie(response, request);
        clearSessionCookies(response, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户。
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    /**
     * 管理员创建用户。
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);

        // 管理端新增用户时分配默认初始密码。
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);

        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员删除用户。
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 管理员更新用户信息。
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据用户 ID 获取原始用户信息。
     * 管理员可查看任意用户，普通用户只能查看自己。
     */
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户并校验查看权限。
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(id)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to view other users");
        }

        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据用户 ID 获取脱敏后的用户视图。
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 管理员分页查询用户列表。
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页查询用户视图列表。
     * 当前接口用于好友搜索场景，登录即可访问。
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 好友搜索场景只要求已登录，不要求管理员权限。
        User loginUser = userService.getLoginUser(request);

        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || size <= 0 || size > 20, ErrorCode.PARAMS_ERROR);

        // 收敛可查询字段，避免通过该接口按敏感字段筛选用户。
        UserQueryRequest safeQueryRequest = new UserQueryRequest();
        BeanUtils.copyProperties(userQueryRequest, safeQueryRequest);
        safeQueryRequest.setId(null);
        safeQueryRequest.setUserRole(null);
        safeQueryRequest.setUserPhone(null);
        safeQueryRequest.setUserEmail(null);
        safeQueryRequest.setWechatId(null);
        safeQueryRequest.setUserAccount(null);

        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(safeQueryRequest);
        // 排除自己和已封禁账号。
        queryWrapper.ne("id", loginUser.getId());
        queryWrapper.ne("user_role", UserConstant.BAN_ROLE);

        // 前端常把关键字放在 userAccount，这里兼容为账号/昵称模糊搜索。
        if (StringUtils.isNotBlank(userQueryRequest.getUserAccount())) {
            String keyword = userQueryRequest.getUserAccount().trim();
            queryWrapper.and(qw -> qw.like("user_account", keyword).or().like("user_name", keyword));
        }

        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);

        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);

        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新当前登录用户的个人资料。
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 搜索用户，支持按昵称或账号模糊查询。
     */
    @GetMapping("/search")
    public BaseResponse<Page<User>> searchUsers(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest request) {

        if (StrUtil.isBlank(keyword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "search keyword cannot be blank");
        }

        // 控制分页大小，避免一次性查询过多数据。
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页大小不能超过 20");
        }

        // 获取当前登录用户。
        User loginUser = userService.getLoginUser(request);

        // 构建查询条件。
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(qw -> qw.like("user_name", keyword).or().like("user_account", keyword));
        queryWrapper.ne("id", loginUser.getId()); // 排除自己
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByDesc("create_time");

        Page<User> userPage = userService.page(new Page<>(current, pageSize), queryWrapper);

        // 脱敏处理，移除敏感字段。
        Page<User> safetyUserPage = new Page<>();
        safetyUserPage.setCurrent(userPage.getCurrent());
        safetyUserPage.setSize(userPage.getSize());
        safetyUserPage.setTotal(userPage.getTotal());
        List<User> safetyUserList = userPage.getRecords().stream()
                .map(user -> {
                    user.setUserPassword(null);
                    return user;
                })
                .collect(Collectors.toList());
        safetyUserPage.setRecords(safetyUserList);

        return ResultUtils.success(safetyUserPage);
    }

    private void writeLoginTokenCookie(HttpServletResponse response, HttpServletRequest request, LoginUserVO loginUserVO) {
        if (response == null || loginUserVO == null || StringUtils.isBlank(loginUserVO.getToken())) {
            return;
        }
        long maxAge = loginUserVO.getTokenExpireSeconds() == null ? 0L : loginUserVO.getTokenExpireSeconds();
        ResponseCookie cookie = ResponseCookie.from("token", loginUserVO.getToken())
                .httpOnly(true)
                .path(resolveCookiePath(request))
                .maxAge(Duration.ofSeconds(Math.max(maxAge, 0L)))
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearLoginTokenCookie(HttpServletResponse response, HttpServletRequest request) {
        if (response == null) {
            return;
        }
        response.addHeader("Set-Cookie", buildExpiredCookie("token", request));
    }

    private void clearSessionCookies(HttpServletResponse response, HttpServletRequest request) {
        if (response == null) {
            return;
        }
        response.addHeader("Set-Cookie", buildExpiredCookie("SESSION", request));
        response.addHeader("Set-Cookie", buildExpiredCookie("JSESSIONID", request));
    }

    private String buildExpiredCookie(String cookieName, HttpServletRequest request) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .path(resolveCookiePath(request))
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build()
                .toString();
    }

    private String resolveCookiePath(HttpServletRequest request) {
        if (request == null || StringUtils.isBlank(request.getContextPath())) {
            return "/";
        }
        return request.getContextPath();
    }
}
