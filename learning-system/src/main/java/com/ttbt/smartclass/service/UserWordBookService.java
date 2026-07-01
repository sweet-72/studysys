package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.dto.userwordbook.UserWordBookQueryRequest;
import com.ttbt.smartclass.model.entity.UserWordBook;
import com.ttbt.smartclass.model.vo.UserWordBookVO;
import java.util.List;

/**
 * 用户单词本服务。
 */
public interface UserWordBookService extends IService<UserWordBook> {

    boolean addToWordBook(Long userId, Long wordId, Integer difficulty);

    boolean removeFromWordBook(Long userId, Long wordId);

    boolean updateLearningStatus(Long userId, Long wordId, Integer learningStatus);

    boolean updateDifficulty(Long userId, Long wordId, Integer difficulty);

    boolean markWordAsCollected(Long userId, Long wordId);

    boolean cancelWordCollected(Long userId, Long wordId);

    List<UserWordBookVO> getUserWordBookList(Long userId, Integer learningStatus, Integer isCollected);

    QueryWrapper<UserWordBook> getQueryWrapper(UserWordBookQueryRequest userWordBookQueryRequest);

    UserWordBookVO getUserWordBookVO(UserWordBook userWordBook);

    List<UserWordBookVO> getUserWordBookVO(List<UserWordBook> userWordBookList);

    int[] getUserWordBookStatistics(Long userId);

    boolean isWordInUserBook(Long userId, Long wordId);
}
