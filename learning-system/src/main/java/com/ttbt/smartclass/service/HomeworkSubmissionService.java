package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.dto.HomeworkGradeRequest;
import com.ttbt.smartclass.model.dto.HomeworkSubmitRequest;
import com.ttbt.smartclass.model.entity.HomeworkSubmission;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionReviewDetailVO;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionReviewPageVO;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionVO;

import java.util.List;

/**
 * 作业提交与批阅服务。
 */
public interface HomeworkSubmissionService extends IService<HomeworkSubmission> {

    Long submitHomework(HomeworkSubmitRequest request, Long userId);

    List<HomeworkSubmissionVO> getSectionExercises(Long sectionId, Long userId);

    Page<HomeworkSubmissionVO> getUngradedHomeworkList(Long current, Long pageSize, Long teacherId);

    void gradeHomework(HomeworkGradeRequest request, Long teacherId);

    Page<HomeworkSubmissionVO> getMyHomeworkList(Long current, Long pageSize, Long userId, Integer status);

    Page<HomeworkSubmissionReviewPageVO> getSubmissionReviewPage(Long current,
                                                                 Long pageSize,
                                                                 Long courseId,
                                                                 Long sectionId,
                                                                 Long homeworkId,
                                                                 Integer reviewStatus,
                                                                 User loginUser);

    HomeworkSubmissionReviewDetailVO getSubmissionReviewDetail(Long submissionId, User loginUser);

    void reviewSubmission(Long submissionId, Integer score, String comment, User loginUser);
}
