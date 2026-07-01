package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.dto.SubmitAnswerRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerResponse;
import com.ttbt.smartclass.model.dto.course.CourseAddRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.CourseCatalogVO;
import com.ttbt.smartclass.model.vo.CourseDetailVO;
import com.ttbt.smartclass.model.vo.CourseProgressVO;
import com.ttbt.smartclass.model.vo.CourseVO;
import com.ttbt.smartclass.model.vo.ExerciseQuestionVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程服务。
 */
public interface CourseService extends IService<Course> {

    CourseCatalogVO getCourseCatalog(Long courseId, Long userId);

    CourseDetailVO getCourseDetail(Long courseId, Long userId);

    List<ExerciseQuestionVO> listPublicExercisesBySection(Long sectionId);

    Course getSectionContent(Long sectionId, Long userId);

    List<Question> getSectionQuestions(Long sectionId);

    SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request);

    CourseProgressVO getCourseProgress(Long courseId, Long userId);

    boolean updateCourseRating(Long courseId, BigDecimal score, int count);

    List<CourseVO> getCoursesByTeacher(Long teacherId, User currentUser);

    Page<CourseVO> getCourseListByPage(Long current, Long pageSize);

    Long createCourse(CourseAddRequest courseAddRequest, User loginUser);

    String uploadVideo(MultipartFile file, User loginUser);

    void assertCanManageCourse(User loginUser, Long courseId);

    /**
     * 学习顺序校验：必须完成上一个小节后才能学习下一个小节。
     */
    void assertSectionLearnable(Long userId, Long sectionId);

    /**
     * 尝试按统一规则将小节标记为已完成。
     *
     * @return true-已满足规则并标记完成（已完成也返回 true），false-当前仅满足学习中但未达到完成条件。
     */
    boolean tryCompleteSection(Long userId, Long sectionId);

    /**
     * 课程推荐：基于学习记录与历史偏好推荐。
     */
    List<CourseVO> recommendCourses(Long userId, Integer limit);

    /**
     * 热门课程推荐：按点击人数、学习人数、评分、创建时间排序。
     */
    List<CourseVO> listHotRecommendCourses(Integer limit);

    /**
     * 记录登录用户课程点击。
     */
    void recordCourseView(Long courseId, Long userId);

    /**
     * 显式开始学习指定小节。
     */
    void startSectionLearning(Long userId, Long courseId, Long sectionId);

    /**
     * 显式完成学习指定小节。
     */
    void completeSectionLearning(Long userId, Long sectionId);
}
