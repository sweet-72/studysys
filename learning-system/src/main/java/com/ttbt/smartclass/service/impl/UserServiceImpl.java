package com.ttbt.smartclass.service.impl;

import static com.ttbt.smartclass.constant.UserConstant.SALT;
import static com.ttbt.smartclass.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.CommonConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.manager.JwtTokenManager;
import com.ttbt.smartclass.mapper.UserMapper;
import com.ttbt.smartclass.model.dto.user.UserQueryRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.enums.UserRoleEnum;
import com.ttbt.smartclass.model.vo.LoginUserVO;
import com.ttbt.smartclass.model.vo.UserVO;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String COLUMN_USER_ACCOUNT = "user_account";
    private static final String COLUMN_USER_PASSWORD = "user_password";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_ROLE = "user_role";
    private static final String COLUMN_USER_GENDER = "user_gender";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_UNION_ID = "union_id";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_UPDATE_TIME = "update_time";
    private static final String COLUMN_IS_DELETE = "is_delete";
    private static final String LOGIN_USER_ID_SESSION_KEY = "LOGIN_USER_ID";
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    private final Map<String, Object> registerLockMap = new ConcurrentHashMap<>();
    private final Map<String, Object> phoneRegisterLockMap = new ConcurrentHashMap<>();
    private final Map<String, Object> wxLoginLockMap = new ConcurrentHashMap<>();

    @Resource
    private JwtTokenManager jwtTokenManager;

    /**
     * 使用账号和密码注册普通用户。
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 新用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验账号、密码和确认密码格式，并确认两次密码一致
        validateAccountRegister(userAccount, userPassword, checkPassword);

        // 按账号加本地锁，避免并发注册同一账号
        Object lock = registerLockMap.computeIfAbsent(userAccount, key -> new Object());
        synchronized (lock) {
            try {
                // 查询账号是否已存在，存在则拒绝重复注册
                if (existsByAccount(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
                }

                // 创建普通用户并保存加密后的密码
                User user = new User();
                user.setUserAccount(userAccount);
                user.setUserName(userAccount);
                user.setUserPassword(encryptPassword(userPassword));
                user.setUserRole(UserRoleEnum.USER.getValue());
                boolean saveResult = this.save(user);
                if (!saveResult) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
                }
                return user.getId();
            } finally {
                // 注册结束后释放当前账号的本地锁对象
                registerLockMap.remove(userAccount);
            }
        }
    }

    /**
     * 使用手机号和密码注册普通用户。
     *
     * @param userPhone 手机号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 新用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long userRegisterByPhone(String userPhone, String userPassword, String checkPassword) {
        // 校验手机号、密码和确认密码格式
        validatePhoneRegister(userPhone, userPassword, checkPassword);

        // 按手机号加本地锁，避免同一手机号并发注册
        Object lock = phoneRegisterLockMap.computeIfAbsent(userPhone, key -> new Object());
        synchronized (lock) {
            try {
                // 手机号已注册时直接拒绝
                if (existsByPhone(userPhone)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被注册");
                }

                // 为手机号注册用户生成唯一随机账号，并保存加密密码
                String userAccount = generateUniqueRandomAccount();
                User user = new User();
                user.setUserAccount(userAccount);
                user.setUserName(buildPhoneDisplayName(userPhone));
                user.setUserPhone(userPhone);
                user.setUserPassword(encryptPassword(userPassword));
                user.setUserRole(UserRoleEnum.USER.getValue());
                boolean saveResult = this.save(user);
                if (!saveResult) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
                }
                return user.getId();
            } finally {
                // 注册结束后释放当前手机号的本地锁对象
                phoneRegisterLockMap.remove(userPhone);
            }
        }
    }

    /**
     * 使用账号密码登录并返回登录用户信息。
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 当前 HTTP 请求
     * @return 登录用户信息和 JWT
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验账号和密码基础格式，避免无效登录请求进入数据库查询
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 按账号查询未删除用户，账号不存在时返回统一参数错误
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ACCOUNT, userAccount);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.last("limit 1");
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount: {} not found", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 校验密码，保存 Session 登录态，并生成包含用户角色的 JWT
        verifyPassword(user, userPassword, COLUMN_USER_ACCOUNT, userAccount);
        saveLoginState(request, user);
        return buildLoginUserVOWithToken(user);
    }

    /**
     * 使用手机号和密码登录并返回登录用户信息。
     *
     * @param userPhone 手机号
     * @param userPassword 用户密码
     * @param request 当前 HTTP 请求
     * @return 登录用户信息和 JWT
     */
    @Override
    public LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request) {
        // 校验手机号和密码格式，防止无效请求进入数据库查询
        if (StringUtils.isAnyBlank(userPhone, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (!userPhone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 按手机号查询未删除用户，找不到时返回统一参数错误
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_PHONE, userPhone);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.last("limit 1");
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userPhone: {} not found", userPhone);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 校验密码，保存 Session 登录态，并生成 JWT 给前端
        verifyPassword(user, userPassword, COLUMN_USER_PHONE, userPhone);
        saveLoginState(request, user);
        return buildLoginUserVOWithToken(user);
    }

    /**
     * 使用微信公众号授权信息登录或自动注册用户。
     *
     * @param wxOAuth2UserInfo 微信授权用户信息
     * @param request 当前 HTTP 请求
     * @return 登录用户信息和 JWT
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        // 微信登录依赖 unionId 识别同一用户，缺失时无法继续
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();

        if (StringUtils.isBlank(unionId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "微信登录失败，未获取到 unionId");
        }

        // 按 unionId 加本地锁，避免首次微信登录时并发创建多个账号
        Object lock = wxLoginLockMap.computeIfAbsent(unionId, key -> new Object());
        synchronized (lock) {
            try {
                // 查询 unionId 绑定的未删除用户
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq(COLUMN_UNION_ID, unionId);
                queryWrapper.eq(COLUMN_IS_DELETE, 0);
                queryWrapper.last("limit 1");
                User user = this.getOne(queryWrapper);

                // 封禁用户禁止通过微信登录
                if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "禁止操作");
                }

                // 首次微信登录时自动创建普通用户，并保存微信头像昵称
                if (user == null) {
                    user = new User();
                    user.setUnionId(unionId);
                    user.setMpOpenId(mpOpenId);
                    user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                    user.setUserName(wxOAuth2UserInfo.getNickname());
                    user.setUserRole(UserRoleEnum.USER.getValue());
                    boolean result = this.save(user);
                    if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
                    }
                }

                // 保存 Session 登录态，并签发 JWT 返回给前端
                saveLoginState(request, user);
                return buildLoginUserVOWithToken(user);
            } finally {
                // 登录流程结束后释放当前 unionId 的本地锁对象
                wxLoginLockMap.remove(unionId);
            }
        }
    }

    /**
     * 获取当前登录用户，未登录时抛出异常。
     *
     * @param request 当前 HTTP 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 优先从 JWT 中解析登录用户，支持前后端分离接口鉴权
        User tokenUser = getLoginUserFromToken(request);
        if (tokenUser != null) {
            return tokenUser;
        }

        // JWT 不存在或无效时，回退到 Session 登录态
        User sessionUser = getLoginUserFromSession(request);
        if (sessionUser != null) {
            return sessionUser;
        }

        // 两种登录态都不存在时按未登录处理
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    /**
     * 获取当前登录用户，未登录时返回 null。
     *
     * @param request 当前 HTTP 请求
     * @return 当前登录用户或 null
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先尝试从 JWT 解析用户，解析失败不抛异常
        User tokenUser = getLoginUserFromToken(request);
        if (tokenUser != null) {
            return tokenUser;
        }
        // 兼容 Session 登录态，仍未登录则返回 null
        return getLoginUserFromSession(request);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        return isAdmin(getLoginUserPermitNull(request));
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 退出登录并清理 JWT 与 Session 登录态。
     *
     * @param request 当前 HTTP 请求
     * @return 是否退出成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 如果请求中携带 JWT，将其加入 Redis 黑名单直到自然过期
        String token = jwtTokenManager.resolveToken(request);
        if (StringUtils.isNotBlank(token)) {
            jwtTokenManager.invalidateToken(token);
        }

        // 请求对象为空时，已尽力处理 token 失效，直接返回成功
        if (request == null) {
            return true;
        }
        // 清理请求属性和 Spring Security 上下文中的登录信息
        request.removeAttribute(USER_LOGIN_STATE);
        request.removeAttribute(LOGIN_USER_ID_SESSION_KEY);
        request.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
        clearSpringSecurityContext();

        // 清理并失效 Session，兼容旧版基于 Session 的登录态
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        try {
            session.removeAttribute(USER_LOGIN_STATE);
            session.removeAttribute(LOGIN_USER_ID_SESSION_KEY);
            session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
            session.invalidate();
        } catch (IllegalStateException e) {
            log.debug("session already invalidated during logout", e);
        }
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        if (user.getBirthday() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(user.getBirthday());
            userVO.setBirthdayYear(calendar.get(Calendar.YEAR));
        } else {
            userVO.setBirthdayYear(0);
        }
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public UserVO getUserVOById(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = this.getById(userId);
        return getUserVO(user);
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        Integer userGender = userQueryRequest.getUserGender();
        String userPhone = userQueryRequest.getUserPhone();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String userEmail = userQueryRequest.getUserEmail();
        String wechatId = userQueryRequest.getWechatId();
        int birthdayYear = userQueryRequest.getBirthdayYear();
        String sortField = normalizeSortField(userQueryRequest.getSortField());
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), COLUMN_USER_ROLE, userRole);
        queryWrapper.eq(StringUtils.isNotBlank(userAccount), COLUMN_USER_ACCOUNT, userAccount);
        queryWrapper.eq(userGender != null, COLUMN_USER_GENDER, userGender);
        queryWrapper.eq(StringUtils.isNotBlank(userPhone), COLUMN_USER_PHONE, userPhone);
        queryWrapper.eq(StringUtils.isNotBlank(userEmail), COLUMN_USER_EMAIL, userEmail);
        queryWrapper.like(StringUtils.isNotBlank(userName), COLUMN_USER_NAME, userName);
        queryWrapper.like(StringUtils.isNotBlank(wechatId), "wechat_id", wechatId);
        queryWrapper.apply(birthdayYear > 0, "YEAR(birthday) = {0}", birthdayYear);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        if (SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, CommonConstant.SORT_ORDER_ASC.equals(sortOrder), normalizeSortField(sortField));
        }
        return queryWrapper;
    }

    @Override
    public List<User> getAllAdmins() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ROLE, UserRoleEnum.ADMIN.getValue());
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        return list(queryWrapper);
    }

    @Override
    public List<User> getFriendList(Long userId) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "好友列表功能尚未接入，请调用 UserFriendService.getFriendList()");
    }

    private void validateAccountRegister(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
    }

    private void validatePhoneRegister(String userPhone, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userPhone, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (!userPhone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
    }

    private boolean existsByAccount(String userAccount) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ACCOUNT, userAccount);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.last("limit 1");
        return this.baseMapper.selectCount(queryWrapper) > 0;
    }

    private boolean existsByPhone(String userPhone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_PHONE, userPhone);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.last("limit 1");
        return this.baseMapper.selectCount(queryWrapper) > 0;
    }

    private String generateUniqueRandomAccount() {
        for (int i = 0; i < 10; i++) {
            String candidate = generateRandomAccount();
            if (!existsByAccount(candidate)) {
                return candidate;
            }
        }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    private String generateRandomAccount() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int index = ThreadLocalRandom.current().nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    private String buildPhoneDisplayName(String userPhone) {
        if (StringUtils.length(userPhone) >= 4) {
            return "用户" + userPhone.substring(userPhone.length() - 4);
        }
        return "新用户";
    }

    private String encryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    private void saveLoginState(HttpServletRequest request, User user) {
        if (request == null || user == null) {
            return;
        }
        Long userId = user.getId();
        if (userId == null) {
            return;
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, userId);
        request.getSession().setAttribute(LOGIN_USER_ID_SESSION_KEY, userId);
    }

    private LoginUserVO buildLoginUserVOWithToken(User user) {
        LoginUserVO loginUserVO = getLoginUserVO(user);
        if (loginUserVO == null) {
            return null;
        }
        loginUserVO.setToken(jwtTokenManager.createToken(user));
        loginUserVO.setTokenExpireSeconds(jwtTokenManager.getExpireSeconds());
        return loginUserVO;
    }

    private User getLoginUserFromToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = jwtTokenManager.resolveToken(request);
        if (StringUtils.isBlank(token)) {
            return null;
        }

        Long userId = jwtTokenManager.getUserIdByToken(token);
        if (userId == null) {
            return null;
        }

        User currentUser = getActiveUserById(userId);
        if (currentUser == null) {
            return null;
        }

        request.setAttribute(USER_LOGIN_STATE, currentUser);
        return currentUser;
    }

    private User getLoginUserFromSession(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object sessionObj = session.getAttribute(USER_LOGIN_STATE);
        Long userId = resolveSessionUserId(sessionObj);
        if (userId == null) {
            Object loginUserIdObj = session.getAttribute(LOGIN_USER_ID_SESSION_KEY);
            userId = resolveSessionUserId(loginUserIdObj);
        }
        if (userId == null) {
            return null;
        }
        return getActiveUserById(userId);
    }

    private void clearSpringSecurityContext() {
        try {
            Class<?> securityContextHolderClass =
                    Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            securityContextHolderClass.getMethod("clearContext").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // Spring Security 未接入时直接忽略
        } catch (Exception e) {
            log.debug("clear spring security context failed", e);
        }
    }

    private Long resolveSessionUserId(Object sessionValue) {
        if (sessionValue == null) {
            return null;
        }
        if (sessionValue instanceof Number) {
            long value = ((Number) sessionValue).longValue();
            return value > 0 ? value : null;
        }
        if (sessionValue instanceof String) {
            String value = ((String) sessionValue).trim();
            if (value.matches("\\d+")) {
                return Long.parseLong(value);
            }
            return null;
        }
        if (sessionValue instanceof Map) {
            Object idObj = ((Map<?, ?>) sessionValue).get("id");
            if (idObj == null) {
                idObj = ((Map<?, ?>) sessionValue).get("user_id");
            }
            if (idObj == null) {
                idObj = ((Map<?, ?>) sessionValue).get("loginUserId");
            }
            return resolveSessionUserId(idObj);
        }
        return null;
    }

    private User getActiveUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        User currentUser = this.getById(userId);
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        if (currentUser.getIsDelete() != null && currentUser.getIsDelete() != 0) {
            return null;
        }
        return currentUser;
    }

    private void verifyPassword(User user, String rawPassword, String identityType, String identityValue) {
        String encryptedInput = encryptPassword(rawPassword);
        String dbPassword = user.getUserPassword();
        if (StringUtils.isBlank(dbPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        boolean passwordMatched = encryptedInput.equals(dbPassword);
        if (!passwordMatched) {
            boolean looksLikeLegacyPlainText = dbPassword.length() != 32 || !dbPassword.matches("[a-f0-9]{32}");
            if (looksLikeLegacyPlainText) {
                passwordMatched = rawPassword.equals(dbPassword);
            }
        }

        if (!passwordMatched) {
            log.info("user login failed, password mismatch for {}: {}", identityType, identityValue);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
    }

    private String normalizeSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return sortField;
        }
        switch (sortField) {
            case "userAccount":
                return COLUMN_USER_ACCOUNT;
            case "userPassword":
                return COLUMN_USER_PASSWORD;
            case "userName":
                return COLUMN_USER_NAME;
            case "userRole":
                return COLUMN_USER_ROLE;
            case "userPhone":
                return COLUMN_USER_PHONE;
            case "userEmail":
                return COLUMN_USER_EMAIL;
            case "userGender":
                return COLUMN_USER_GENDER;
            case "createTime":
            case "create_time":
                return COLUMN_CREATE_TIME;
            case "updateTime":
            case "update_time":
                return COLUMN_UPDATE_TIME;
            case "isDelete":
            case "is_delete":
                return COLUMN_IS_DELETE;
            default:
                return SqlUtils.normalizeSortField(sortField);
        }
    }
}

