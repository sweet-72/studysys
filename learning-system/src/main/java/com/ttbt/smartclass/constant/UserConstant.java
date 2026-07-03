package com.ttbt.smartclass.constant;

/**
 * 用户常量
*/
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 学生角色
     */
    String STUDENT_ROLE = "student";

    /**
     * 讲师角色
     */
    String TEACHER_ROLE = "teacher";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 课程内容维护：管理员或讲师（用于 @AuthCheck，逗号分隔多角色）
     */
    String ADMIN_OR_TEACHER_ROLES = "admin,teacher";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion
}
