package com.ttbt.smartclass.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.CommonConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.DailyWordMapper;
import com.ttbt.smartclass.mapper.UserWordBookMapper;
import com.ttbt.smartclass.model.dto.userwordbook.UserWordBookQueryRequest;
import com.ttbt.smartclass.model.entity.DailyWord;
import com.ttbt.smartclass.model.entity.UserWordBook;
import com.ttbt.smartclass.model.vo.UserWordBookVO;
import com.ttbt.smartclass.service.DailyWordService;
import com.ttbt.smartclass.service.UserWordBookService;
import com.ttbt.smartclass.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户单词本服务实现类。
 * 负责单词本添加、逻辑删除、学习状态、难度和收藏状态维护。
 */
@Service
@Slf4j
public class UserWordBookServiceImpl extends ServiceImpl<UserWordBookMapper, UserWordBook>
        implements UserWordBookService {

    private static final int DEFAULT_DIFFICULTY = 1;

    @Resource
    private DailyWordMapper dailyWordMapper;

    @Resource
    private DailyWordService dailyWordService;

    /**
     * 将单词加入用户单词本。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @param difficulty 单词难度
     * @return 是否添加成功
     */
    @Override
    public boolean addToWordBook(Long userId, Long wordId, Integer difficulty) {
        // 校验用户和单词参数，并确认每日单词存在
        if (userId == null || wordId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id or word_id is null");
        }
        validateWordExists(wordId);

        // 查询用户单词本历史记录，避免重复新增同一单词
        UserWordBook userWordBook = getUserWordBookRecord(userId, wordId);
        int normalizedDifficulty = normalizeDifficulty(difficulty);
        Date now = new Date();
        // 已存在且未删除时直接视为添加成功
        if (userWordBook != null && isActive(userWordBook)) {
            return true;
        }
        // 之前删除过的记录恢复为有效，并重置难度和学习状态
        if (userWordBook != null) {
            userWordBook.setIsDeleted(0);
            userWordBook.setDifficulty(normalizedDifficulty);
            userWordBook.setLearningStatus(0);
            userWordBook.setUpdateTime(now);
            return this.updateById(userWordBook);
        }

        // 首次加入单词本时创建新记录，默认未学习且未收藏
        UserWordBook newUserWordBook = new UserWordBook();
        newUserWordBook.setUserId(userId);
        newUserWordBook.setWordId(wordId);
        newUserWordBook.setLearningStatus(0);
        newUserWordBook.setIsCollected(0);
        newUserWordBook.setDifficulty(normalizedDifficulty);
        newUserWordBook.setIsDeleted(0);
        newUserWordBook.setCreateTime(now);
        newUserWordBook.setUpdateTime(now);
        return this.save(newUserWordBook);
    }

    /**
     * 从用户单词本中逻辑移除单词。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @return 是否移除成功
     */
    @Override
    public boolean removeFromWordBook(Long userId, Long wordId) {
        // 校验用户和单词参数，逻辑删除必须定位到用户自己的记录
        if (userId == null || wordId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id or word_id is null");
        }

        // 使用 updateWrapper 按用户和单词维度设置删除标记
        UpdateWrapper<UserWordBook> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .set("is_deleted", 1)
                .set("update_time", new Date());
        return this.update(updateWrapper);
    }

    /**
     * 更新用户单词本中单词的学习状态。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @param learningStatus 学习状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateLearningStatus(Long userId, Long wordId, Integer learningStatus) {
        // 校验必要参数，学习状态缺失时不能更新
        if (userId == null || wordId == null || learningStatus == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id, word_id or learning_status is null");
        }

        // 只更新当前用户未删除单词本记录的学习状态
        UpdateWrapper<UserWordBook> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .eq("is_deleted", 0)
                .set("learning_status", learningStatus)
                .set("update_time", new Date());
        return this.update(updateWrapper);
    }

    /**
     * 更新用户单词本中单词的难度。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @param difficulty 难度值
     * @return 是否更新成功
     */
    @Override
    public boolean updateDifficulty(Long userId, Long wordId, Integer difficulty) {
        // 校验必要参数，难度缺失时不能更新
        if (userId == null || wordId == null || difficulty == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id, word_id or difficulty is null");
        }

        // 只更新当前用户未删除单词本记录的难度
        UpdateWrapper<UserWordBook> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .eq("is_deleted", 0)
                .set("difficulty", difficulty)
                .set("update_time", new Date());
        return this.update(updateWrapper);
    }

    /**
     * 将单词标记为用户收藏单词。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @return 是否标记成功
     */
    @Override
    public boolean markWordAsCollected(Long userId, Long wordId) {
        // 校验用户和单词参数，并确认单词存在
        if (userId == null || wordId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id or word_id is null");
        }
        validateWordExists(wordId);

        // 没有单词本记录时，创建一条收藏状态的记录
        UserWordBook userWordBook = getUserWordBookRecord(userId, wordId);
        Date now = new Date();
        if (userWordBook == null) {
            UserWordBook newUserWordBook = new UserWordBook();
            newUserWordBook.setUserId(userId);
            newUserWordBook.setWordId(wordId);
            newUserWordBook.setLearningStatus(0);
            newUserWordBook.setIsCollected(1);
            newUserWordBook.setCollectedTime(now);
            newUserWordBook.setDifficulty(DEFAULT_DIFFICULTY);
            newUserWordBook.setIsDeleted(0);
            newUserWordBook.setCreateTime(now);
            newUserWordBook.setUpdateTime(now);
            return this.save(newUserWordBook);
        }
        // 已经是有效收藏记录时直接返回成功
        if (isActive(userWordBook) && Integer.valueOf(1).equals(userWordBook.getIsCollected())) {
            return true;
        }

        // 恢复或更新已有记录为收藏状态，并补齐默认学习状态和难度
        userWordBook.setIsDeleted(0);
        userWordBook.setLearningStatus(userWordBook.getLearningStatus() == null ? 0 : userWordBook.getLearningStatus());
        userWordBook.setDifficulty(normalizeDifficulty(userWordBook.getDifficulty()));
        userWordBook.setIsCollected(1);
        userWordBook.setCollectedTime(now);
        userWordBook.setUpdateTime(now);
        return this.updateById(userWordBook);
    }

    /**
     * 取消用户单词收藏状态。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @return 是否取消成功
     */
    @Override
    public boolean cancelWordCollected(Long userId, Long wordId) {
        // 校验用户和单词参数，取消收藏必须定位到用户自己的记录
        if (userId == null || wordId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id or word_id is null");
        }

        // 记录不存在、已删除或本来未收藏时视为取消成功
        UserWordBook userWordBook = getUserWordBookRecord(userId, wordId);
        if (userWordBook == null || !isActive(userWordBook) || !Integer.valueOf(1).equals(userWordBook.getIsCollected())) {
            return true;
        }

        // 清除收藏标记和收藏时间，保留单词本记录本身
        userWordBook.setIsCollected(0);
        userWordBook.setCollectedTime(null);
        userWordBook.setUpdateTime(new Date());
        return this.updateById(userWordBook);
    }

    @Override
    public List<UserWordBookVO> getUserWordBookList(Long userId, Integer learningStatus, Integer isCollected) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id is null");
        }

        QueryWrapper<UserWordBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_deleted", 0);
        if (learningStatus != null) {
            queryWrapper.eq("learning_status", learningStatus);
        }
        if (isCollected != null) {
            queryWrapper.eq("is_collected", isCollected);
        }
        queryWrapper.orderByDesc("update_time");
        return this.getUserWordBookVO(this.list(queryWrapper));
    }

    @Override
    public QueryWrapper<UserWordBook> getQueryWrapper(UserWordBookQueryRequest userWordBookQueryRequest) {
        if (userWordBookQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "query request is null");
        }

        QueryWrapper<UserWordBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userWordBookQueryRequest.getId() != null, "id", userWordBookQueryRequest.getId())
                .eq(userWordBookQueryRequest.getUserId() != null, "user_id", userWordBookQueryRequest.getUserId())
                .eq(userWordBookQueryRequest.getWordId() != null, "word_id", userWordBookQueryRequest.getWordId())
                .eq(userWordBookQueryRequest.getLearningStatus() != null, "learning_status", userWordBookQueryRequest.getLearningStatus())
                .eq(userWordBookQueryRequest.getIsCollected() != null, "is_collected", userWordBookQueryRequest.getIsCollected())
                .eq(userWordBookQueryRequest.getDifficulty() != null, "difficulty", userWordBookQueryRequest.getDifficulty())
                .eq(userWordBookQueryRequest.getCreateTime() != null, "create_time", userWordBookQueryRequest.getCreateTime())
                .ge(userWordBookQueryRequest.getCreateTimeStart() != null, "create_time", userWordBookQueryRequest.getCreateTimeStart())
                .le(userWordBookQueryRequest.getCreateTimeEnd() != null, "create_time", userWordBookQueryRequest.getCreateTimeEnd())
                .eq("is_deleted", 0);

        if (StringUtils.isNotBlank(userWordBookQueryRequest.getWord())) {
            List<Long> wordIds = getWordIdsByContent(userWordBookQueryRequest.getWord());
            if (wordIds.isEmpty()) {
                queryWrapper.eq("id", -1);
            } else {
                queryWrapper.in("word_id", wordIds);
            }
        }

        String sortField = userWordBookQueryRequest.getSortField();
        String sortOrder = userWordBookQueryRequest.getSortOrder();
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                SqlUtils.normalizeSortField(sortField));
        return queryWrapper;
    }

    private List<Long> getWordIdsByContent(String word) {
        if (StringUtils.isBlank(word)) {
            return new ArrayList<>();
        }
        QueryWrapper<DailyWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("word", word)
                .eq("is_delete", 0)
                .select("id");
        return dailyWordMapper.selectList(queryWrapper).stream()
                .map(DailyWord::getId)
                .collect(Collectors.toList());
    }

    @Override
    public UserWordBookVO getUserWordBookVO(UserWordBook userWordBook) {
        if (userWordBook == null) {
            return null;
        }

        UserWordBookVO userWordBookVO = new UserWordBookVO();
        BeanUtils.copyProperties(userWordBook, userWordBookVO);
        Long wordId = userWordBook.getWordId();
        if (wordId != null) {
            DailyWord dailyWord = dailyWordMapper.selectById(wordId);
            if (dailyWord != null) {
                userWordBookVO.setWord(dailyWord.getWord());
                userWordBookVO.setTranslation(dailyWord.getTranslation());
                userWordBookVO.setPhonetic(dailyWord.getPronunciation());
                userWordBookVO.setPronunciation(dailyWord.getAudioUrl());
                userWordBookVO.setExample(dailyWord.getExample());
            } else {
                log.warn("daily word not found, word_id={}", wordId);
                userWordBookVO.setWord("Unknown Word");
                userWordBookVO.setTranslation("No Translation");
            }
        }
        return userWordBookVO;
    }

    @Override
    public List<UserWordBookVO> getUserWordBookVO(List<UserWordBook> userWordBookList) {
        if (CollUtil.isEmpty(userWordBookList)) {
            return new ArrayList<>();
        }
        return userWordBookList.stream().map(this::getUserWordBookVO).collect(Collectors.toList());
    }

    @Override
    public int[] getUserWordBookStatistics(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id is null");
        }

        int[] stats = new int[3];

        QueryWrapper<UserWordBook> totalQuery = new QueryWrapper<>();
        totalQuery.eq("user_id", userId).eq("is_deleted", 0);
        stats[0] = Math.toIntExact(this.count(totalQuery));

        QueryWrapper<UserWordBook> learnedQuery = new QueryWrapper<>();
        learnedQuery.eq("user_id", userId).eq("is_deleted", 0).eq("learning_status", 1);
        stats[1] = Math.toIntExact(this.count(learnedQuery));

        QueryWrapper<UserWordBook> toReviewQuery = new QueryWrapper<>();
        toReviewQuery.eq("user_id", userId).eq("is_deleted", 0).ne("learning_status", 2);
        stats[2] = Math.toIntExact(this.count(toReviewQuery));
        return stats;
    }

    /**
     * 判断单词是否在用户单词本中。
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @return 是否存在未删除记录
     */
    @Override
    public boolean isWordInUserBook(Long userId, Long wordId) {
        // 参数缺失时直接返回不存在
        if (userId == null || wordId == null) {
            return false;
        }

        // 只统计未删除的用户单词本记录
        QueryWrapper<UserWordBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .eq("is_deleted", 0);
        return this.count(queryWrapper) > 0;
    }

    private UserWordBook getUserWordBookRecord(Long userId, Long wordId) {
        QueryWrapper<UserWordBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .last("limit 1");
        return this.getOne(queryWrapper, false);
    }

    private void validateWordExists(Long wordId) {
        DailyWord dailyWord = dailyWordService.getById(wordId);
        if (dailyWord == null || (dailyWord.getIsDelete() != null && dailyWord.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "daily word not found");
        }
    }

    private boolean isActive(UserWordBook userWordBook) {
        return userWordBook.getIsDeleted() == null || userWordBook.getIsDeleted() == 0;
    }

    private int normalizeDifficulty(Integer difficulty) {
        return difficulty == null ? DEFAULT_DIFFICULTY : difficulty;
    }
}
