package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.HomeworkGradeRequest;
import com.ttbt.smartclass.model.dto.course.CourseManageCreateRequest;
import com.ttbt.smartclass.model.dto.course.CourseUpdateRequest;
import com.ttbt.smartclass.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 课程后台管理服务
 */
public interface CourseManageAdminService {

    /**
     * 创建课程（含章节与小节）
     */
    Long createCourse(CourseManageCreateRequest request, User loginUser);

    /**
     * 更新课程（支持章节小节结构重建）
     */
    boolean updateCourse(CourseUpdateRequest request, User loginUser);

    /**
     * 删除课程（逻辑删除）
     */
    boolean deleteCourse(Long courseId, User loginUser);

    /**
     * 上传视频或录入视频URL
     */
    String handleVideoUpload(MultipartFile file, String videoUrl, User loginUser);

    /**
     * 批改作业
     */
    void reviewHomework(HomeworkGradeRequest request, User loginUser);
}
