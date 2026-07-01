package com.ttbt.smartclass.service.impl;

import static com.ttbt.smartclass.constant.UserConstant.USER_LOGIN_STATE;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ttbt.smartclass.manager.JwtTokenManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

class UserServiceImplLogoutTest {

    private static final String LOGIN_USER_ID_SESSION_KEY = "LOGIN_USER_ID";
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    private UserServiceImpl userService;
    private JwtTokenManager jwtTokenManager;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        jwtTokenManager = mock(JwtTokenManager.class);
        ReflectionTestUtils.setField(userService, "jwtTokenManager", jwtTokenManager);
    }

    @Test
    void userLogoutShouldInvalidateSessionAndClearAttributes() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(USER_LOGIN_STATE, 1L);
        session.setAttribute(LOGIN_USER_ID_SESSION_KEY, 1L);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, "security-context");
        request.setSession(session);
        when(jwtTokenManager.resolveToken(request)).thenReturn("logout-token");

        boolean result = userService.userLogout(request);

        assertTrue(result);
        verify(jwtTokenManager).invalidateToken("logout-token");
        assertNull(request.getSession(false), "退出后不应保留旧 session");
    }

    @Test
    void getLoginUserPermitNullShouldNotCreateSessionWhenNoLoginState() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(jwtTokenManager.resolveToken(request)).thenReturn(null);

        assertNull(userService.getLoginUserPermitNull(request));
        assertNull(request.getSession(false), "未登录读取当前用户时不应创建新 session");
    }
}
