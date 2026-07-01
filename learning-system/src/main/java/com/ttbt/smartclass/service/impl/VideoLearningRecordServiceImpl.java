package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.mapper.UserCourseMapper;
import com.ttbt.smartclass.mapper.VideoLearningRecordMapper;
import com.ttbt.smartclass.model.dto.VideoProgressSaveRequest;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.UserCourse;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.VideoLearningRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 视频学习记录服务实现类
 */
@Slf4j
@Service
public class VideoLearningRecordServiceImpl extends ServiceImpl<VideoLearningRecordMapper, VideoLearningRecord>
        implements VideoLearningRecordService {

    @Resource
    private SectionService sectionService;

    @Resource
    private CourseChapterMapper courseChapterMapper;

    @Resource
    private UserCourseMapper userCourseMapper;

    @Resource
    private CourseService courseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveVideoProgress(VideoProgressSaveRequest request, Long userId) {
        if (request == null || request.getSectionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sectionId is required");
        }

        Section section = sectionService.getById(request.getSectionId());
        if (section == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        courseService.assertSectionLearnable(userId, request.getSectionId());

        LambdaQueryWrapper<VideoLearningRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoLearningRecord::getUserId, userId)
                .eq(VideoLearningRecord::getSectionId, request.getSectionId());
        VideoLearningRecord record = this.getOne(queryWrapper);

        int durationSec = section.getVideoDuration() != null ? section.getVideoDuration() : 0;
        int lastPos = Math.max(0, request.getLastWatchPosition() == null ? 0 : request.getLastWatchPosition());
        int learned = Math.max(0, request.getLearnedTime() == null ? 0 : request.getLearnedTime());

        if (record == null) {
            record = new VideoLearningRecord();
            record.setUserId(userId);
            record.setSectionId(request.getSectionId());
            record.setVideoDuration(section.getVideoDuration());
            record.setLearnedTime(learned);
            record.setLastWatchPosition(lastPos);
            record.setIsCompleted(request.getIsCompleted() != null && request.getIsCompleted() == 1 ? 1 : 0);
            record.setProgress(computeProgressPercent(lastPos, durationSec));
            this.save(record);
        } else {
            int mergedLearned = Math.max(record.getLearnedTime() != null ? record.getLearnedTime() : 0, learned);
            int mergedPos = Math.max(record.getLastWatchPosition() != null ? record.getLastWatchPosition() : 0, lastPos);
            record.setLearnedTime(mergedLearned);
            record.setLastWatchPosition(mergedPos);
            int completed = (request.getIsCompleted() != null && request.getIsCompleted() == 1) ? 1
                    : (record.getIsCompleted() != null ? record.getIsCompleted() : 0);
            if (durationSec > 0 && mergedPos >= durationSec) {
                completed = 1;
            }
            record.setIsCompleted(completed);
            record.setProgress(computeProgressPercent(mergedPos, durationSec));
            this.updateById(record);
        }

        touchUserCourseLearning(userId, section.getChapterId());
        courseService.tryCompleteSection(userId, request.getSectionId());

        log.info("用户 {} 保存视频学习进度，sectionId={}, progress={}%",
                userId, request.getSectionId(), record.getProgress());
    }

    private static BigDecimal computeProgressPercent(int lastWatchPosition, int videoDurationSec) {
        if (videoDurationSec <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(lastWatchPosition)
                .divide(BigDecimal.valueOf(videoDurationSec), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .min(BigDecimal.valueOf(100));
    }

    /**
     * 同步用户课程关系的最近学习时间，用于我的课程排序和学习状态展示。
     */
    private void touchUserCourseLearning(Long userId, Long chapterId) {
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return;
        }
        Long courseId = chapter.getCourseId();
        LambdaQueryWrapper<UserCourse> qw = new LambdaQueryWrapper<>();
        qw.eq(UserCourse::getUserId, userId).eq(UserCourse::getCourseId, courseId);
        UserCourse uc = userCourseMapper.selectOne(qw);
        Date now = new Date();
        if (uc == null) {
            uc = new UserCourse();
            uc.setUserId(userId);
            uc.setCourseId(courseId);
            uc.setStatus(0);
            uc.setProgress(BigDecimal.ZERO);
            uc.setStartTime(now);
            uc.setLastLearnTime(now);
            userCourseMapper.insert(uc);
            return;
        }
        uc.setLastLearnTime(now);
        userCourseMapper.updateById(uc);
    }

    @Override
    public VideoLearningRecord getVideoProgress(Long sectionId, Long userId) {
        LambdaQueryWrapper<VideoLearningRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoLearningRecord::getUserId, userId)
                .eq(VideoLearningRecord::getSectionId, sectionId);
        return this.getOne(queryWrapper);
    }
}
