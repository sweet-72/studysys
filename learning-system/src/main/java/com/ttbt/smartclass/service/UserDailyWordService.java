package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordReviewUpdateRequest;
import com.ttbt.smartclass.model.entity.UserDailyWord;
import com.ttbt.smartclass.model.vo.DailyWordReviewItemVO;
import com.ttbt.smartclass.model.vo.DailyWordStudyRecordVO;

/**
 * 用户每日单词学习关系服务。
 */
public interface UserDailyWordService extends IService<UserDailyWord> {

    boolean markWordAsStudied(long wordId, long userId);

    boolean cancelWordStudied(long wordId, long userId);

    boolean updateMasteryLevel(long wordId, long userId, int masteryLevel);

    boolean saveWordNote(long wordId, long userId, String noteContent);

    UserDailyWord getUserDailyWord(long wordId, long userId);

    Page<DailyWordStudyRecordVO> listStudyRecordPage(long userId, long current, long pageSize);

    boolean markWordAsWrong(long wordId, long userId);

    boolean cancelWordWrong(long wordId, long userId);

    Page<DailyWordReviewItemVO> listWrongWordPage(long userId, long current, long pageSize);

    Page<DailyWordReviewItemVO> listReviewWordPage(long userId, long current, long pageSize);

    boolean updateReviewResult(long wordId, long userId, DailyWordReviewUpdateRequest reviewUpdateRequest);
}
