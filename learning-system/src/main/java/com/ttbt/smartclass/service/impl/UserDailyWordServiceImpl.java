package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.UserDailyWordMapper;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordReviewUpdateRequest;
import com.ttbt.smartclass.model.entity.DailyWord;
import com.ttbt.smartclass.model.entity.UserDailyWord;
import com.ttbt.smartclass.model.entity.UserWordBook;
import com.ttbt.smartclass.model.vo.DailyWordReviewItemVO;
import com.ttbt.smartclass.model.vo.DailyWordStudyRecordVO;
import com.ttbt.smartclass.service.DailyWordService;
import com.ttbt.smartclass.service.UserDailyWordService;
import com.ttbt.smartclass.service.UserWordBookService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户每日单词学习关系服务实现类。
 * 负责每日单词学习状态、掌握度、错词、笔记和复习列表维护。
 */
@Service
public class UserDailyWordServiceImpl extends ServiceImpl<UserDailyWordMapper, UserDailyWord>
        implements UserDailyWordService {

    private static final int MASTERY_THRESHOLD_FOR_REVIEW = 4;

    @Resource
    private DailyWordService dailyWordService;

    @Resource
    private UserWordBookService userWordBookService;

    /**
     * 标记用户已学习指定每日单词。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @return 是否保存成功
     */
    @Override
    public boolean markWordAsStudied(long wordId, long userId) {
        // 获取或创建用户单词学习记录，并写入学习时间
        UserDailyWord userDailyWord = getOrCreateUserDailyWord(userId, wordId, false);
        Date now = new Date();
        userDailyWord.setIsStudied(1);
        userDailyWord.setStudyTime(now);
        userDailyWord.setUpdateTime(now);
        return saveOrUpdateUserDailyWord(userDailyWord);
    }

    /**
     * 取消用户指定每日单词的已学状态。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @return 是否更新成功
     */
    @Override
    public boolean cancelWordStudied(long wordId, long userId) {
        // 没有学习记录时视为已经取消成功
        UserDailyWord userDailyWord = getUserDailyWord(wordId, userId);
        if (userDailyWord == null) {
            return true;
        }
        // 仅清除已学标记并刷新更新时间，保留笔记、错词等其他信息
        userDailyWord.setIsStudied(0);
        userDailyWord.setUpdateTime(new Date());
        return this.updateById(userDailyWord);
    }

    /**
     * 更新用户对指定每日单词的掌握度。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @param masteryLevel 掌握度等级
     * @return 是否更新成功
     */
    @Override
    public boolean updateMasteryLevel(long wordId, long userId, int masteryLevel) {
        // 校验掌握度范围，并确保用户单词学习记录存在
        validateMasteryLevel(masteryLevel);
        UserDailyWord userDailyWord = getOrCreateUserDailyWord(userId, wordId, false);
        Date now = new Date();
        userDailyWord.setMasteryLevel(masteryLevel);
        // 首次更新掌握度时同步标记为已学习
        if (userDailyWord.getIsStudied() == null || userDailyWord.getIsStudied() != 1) {
            userDailyWord.setIsStudied(1);
            userDailyWord.setStudyTime(now);
        }
        userDailyWord.setUpdateTime(now);
        // 掌握度达到复习阈值后自动移出错词列表
        if (masteryLevel >= MASTERY_THRESHOLD_FOR_REVIEW) {
            userDailyWord.setIsWrong(0);
            userDailyWord.setWrongTime(null);
        }
        return saveOrUpdateUserDailyWord(userDailyWord);
    }

    /**
     * 保存用户每日单词学习笔记。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @param noteContent 笔记内容
     * @return 是否保存成功
     */
    @Override
    public boolean saveWordNote(long wordId, long userId, String noteContent) {
        // 获取或创建用户单词记录，并更新笔记内容和笔记时间
        UserDailyWord userDailyWord = getOrCreateUserDailyWord(userId, wordId, false);
        Date now = new Date();
        userDailyWord.setNoteContent(noteContent);
        userDailyWord.setNoteTime(now);
        userDailyWord.setUpdateTime(now);
        return saveOrUpdateUserDailyWord(userDailyWord);
    }

    @Override
    public UserDailyWord getUserDailyWord(long wordId, long userId) {
        QueryWrapper<UserDailyWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .last("limit 1");
        return this.getOne(queryWrapper, false);
    }

    /**
     * 分页查询用户每日单词学习记录。
     *
     * @param userId 用户 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @return 学习记录分页数据
     */
    @Override
    public Page<DailyWordStudyRecordVO> listStudyRecordPage(long userId, long current, long pageSize) {
        // 按更新时间、学习时间和记录 id 倒序查询用户学习记录
        QueryWrapper<UserDailyWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("update_time")
                .orderByDesc("study_time")
                .orderByDesc("id");

        Page<UserDailyWord> userDailyWordPage = this.page(new Page<>(current, pageSize), queryWrapper);
        Page<DailyWordStudyRecordVO> resultPage = new Page<>(
                userDailyWordPage.getCurrent(),
                userDailyWordPage.getSize(),
                userDailyWordPage.getTotal());

        // 当前页为空时直接返回空分页，避免后续批量查询单词信息
        List<UserDailyWord> records = userDailyWordPage.getRecords();
        if (records == null || records.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        // 批量加载每日单词和单词本信息，避免逐条查询
        WordContext wordContext = buildWordContext(userId, records.stream()
                .map(UserDailyWord::getWordId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet()));

        // 将学习记录、单词基础信息和收藏状态合并为前端展示 VO
        List<DailyWordStudyRecordVO> voList = records.stream()
                .map(record -> toStudyRecordVO(record, wordContext.dailyWordMap.get(record.getWordId()), wordContext.userWordBookMap.get(record.getWordId())))
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 将指定每日单词标记为用户错词。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @return 是否标记成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markWordAsWrong(long wordId, long userId) {
        // 获取或创建用户单词记录，并写入错词标记和错词时间
        UserDailyWord userDailyWord = getOrCreateUserDailyWord(userId, wordId, false);
        Date now = new Date();
        userDailyWord.setIsWrong(1);
        userDailyWord.setWrongTime(now);
        userDailyWord.setUpdateTime(now);
        return saveOrUpdateUserDailyWord(userDailyWord);
    }

    /**
     * 取消用户指定每日单词的错词状态。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelWordWrong(long wordId, long userId) {
        // 没有学习记录时视为已经取消错词成功
        UserDailyWord userDailyWord = getUserDailyWord(wordId, userId);
        if (userDailyWord == null) {
            return true;
        }
        // 清除错词状态和错词时间，保留其他学习信息
        userDailyWord.setIsWrong(0);
        userDailyWord.setWrongTime(null);
        userDailyWord.setUpdateTime(new Date());
        return this.updateById(userDailyWord);
    }

    /**
     * 分页查询用户错词列表。
     *
     * @param userId 用户 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @return 错词分页数据
     */
    @Override
    public Page<DailyWordReviewItemVO> listWrongWordPage(long userId, long current, long pageSize) {
        // 只查询当前用户标记为错词的记录，并按错词时间倒序排列
        QueryWrapper<UserDailyWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_wrong", 1)
                .orderByDesc("wrong_time")
                .orderByDesc("update_time")
                .orderByDesc("id");

        Page<UserDailyWord> wrongPage = this.page(new Page<>(current, pageSize), queryWrapper);
        Page<DailyWordReviewItemVO> resultPage = new Page<>(wrongPage.getCurrent(), wrongPage.getSize(), wrongPage.getTotal());
        if (wrongPage.getRecords() == null || wrongPage.getRecords().isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        // 批量补充单词基础信息和单词本收藏状态
        WordContext wordContext = buildWordContext(userId, wrongPage.getRecords().stream()
                .map(UserDailyWord::getWordId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet()));
        // 将错词记录转换为复习项 VO
        List<DailyWordReviewItemVO> voList = wrongPage.getRecords().stream()
                .map(record -> toReviewItemVO(record, wordContext.dailyWordMap.get(record.getWordId()), wordContext.userWordBookMap.get(record.getWordId())))
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 分页查询用户需要复习的单词列表。
     *
     * @param userId 用户 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @return 复习单词分页数据
     */
    @Override
    public Page<DailyWordReviewItemVO> listReviewWordPage(long userId, long current, long pageSize) {
        // 查询用户学习记录，后续筛选错词和低掌握度单词进入复习列表
        List<UserDailyWord> userDailyWords = this.list(new QueryWrapper<UserDailyWord>()
                .eq("user_id", userId)
                .orderByDesc("review_time")
                .orderByDesc("update_time")
                .orderByDesc("study_time")
                .orderByDesc("id"));
        // 查询用户收藏的单词，收藏但未学习的单词也纳入复习列表
        List<UserWordBook> collectedWordBooks = userWordBookService.list(new QueryWrapper<UserWordBook>()
                .eq("user_id", userId)
                .eq("is_deleted", 0)
                .eq("is_collected", 1)
                .orderByDesc("collected_time")
                .orderByDesc("update_time")
                .orderByDesc("id"));

        // 按 wordId 合并学习记录和收藏记录，避免同一单词重复出现
        LinkedHashMap<Long, UserDailyWord> reviewRecordMap = new LinkedHashMap<>();
        for (UserDailyWord record : userDailyWords) {
            if (record == null || record.getWordId() == null || record.getWordId() <= 0) {
                continue;
            }
            if (shouldJoinReviewList(record)) {
                reviewRecordMap.put(record.getWordId(), record);
            }
        }
        for (UserWordBook userWordBook : collectedWordBooks) {
            if (userWordBook == null || userWordBook.getWordId() == null || userWordBook.getWordId() <= 0) {
                continue;
            }
            reviewRecordMap.putIfAbsent(userWordBook.getWordId(), buildVirtualReviewRecord(userWordBook));
        }

        // 按复习时间、错词时间、更新时间和学习时间综合排序后手动分页
        List<UserDailyWord> mergedRecords = new ArrayList<>(reviewRecordMap.values());
        mergedRecords.sort((left, right) -> compareReviewPriority(right, left));

        List<UserDailyWord> pageRecords = paginateList(mergedRecords, current, pageSize);
        Page<DailyWordReviewItemVO> resultPage = new Page<>(current, pageSize, mergedRecords.size());
        if (pageRecords.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        // 批量补充单词基础信息和收藏状态，再转换为复习项 VO
        WordContext wordContext = buildWordContext(userId, pageRecords.stream()
                .map(UserDailyWord::getWordId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet()));
        List<DailyWordReviewItemVO> voList = pageRecords.stream()
                .map(record -> toReviewItemVO(record, wordContext.dailyWordMap.get(record.getWordId()), wordContext.userWordBookMap.get(record.getWordId())))
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 更新用户每日单词复习结果。
     *
     * @param wordId 单词 ID
     * @param userId 用户 ID
     * @param reviewUpdateRequest 复习结果请求
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReviewResult(long wordId, long userId, DailyWordReviewUpdateRequest reviewUpdateRequest) {
        // 复习结果必须关联真实有效的每日单词，并确保用户学习记录存在
        UserDailyWord userDailyWord = getOrCreateUserDailyWord(userId, wordId, true);
        Date now = new Date();
        userDailyWord.setIsStudied(1);
        if (userDailyWord.getStudyTime() == null) {
            userDailyWord.setStudyTime(now);
        }
        userDailyWord.setReviewTime(now);
        userDailyWord.setUpdateTime(now);

        // 前端传入掌握度时校验并更新；达到阈值后自动清除错词标记
        if (reviewUpdateRequest != null && reviewUpdateRequest.getMasteryLevel() != null) {
            int masteryLevel = reviewUpdateRequest.getMasteryLevel();
            validateMasteryLevel(masteryLevel);
            userDailyWord.setMasteryLevel(masteryLevel);
            if (masteryLevel >= MASTERY_THRESHOLD_FOR_REVIEW) {
                userDailyWord.setIsWrong(0);
                userDailyWord.setWrongTime(null);
            }
        }
        // 前端明确要求移出错词时，清除错词状态和错词时间
        if (reviewUpdateRequest != null && Boolean.TRUE.equals(reviewUpdateRequest.getRemoveWrong())) {
            userDailyWord.setIsWrong(0);
            userDailyWord.setWrongTime(null);
        }
        return saveOrUpdateUserDailyWord(userDailyWord);
    }

    private boolean shouldJoinReviewList(UserDailyWord record) {
        if (record == null) {
            return false;
        }
        if (Integer.valueOf(1).equals(record.getIsWrong())) {
            return true;
        }
        return Integer.valueOf(1).equals(record.getIsStudied())
                && (record.getMasteryLevel() == null || record.getMasteryLevel() < MASTERY_THRESHOLD_FOR_REVIEW);
    }

    private UserDailyWord buildVirtualReviewRecord(UserWordBook userWordBook) {
        UserDailyWord virtualRecord = new UserDailyWord();
        virtualRecord.setUserId(userWordBook.getUserId());
        virtualRecord.setWordId(userWordBook.getWordId());
        virtualRecord.setStudyTime(null);
        virtualRecord.setReviewTime(null);
        virtualRecord.setUpdateTime(userWordBook.getUpdateTime());
        virtualRecord.setIsWrong(0);
        virtualRecord.setMasteryLevel(null);
        return virtualRecord;
    }

    private int compareReviewPriority(UserDailyWord left, UserDailyWord right) {
        int byReviewTime = compareNullableDate(left.getReviewTime(), right.getReviewTime());
        if (byReviewTime != 0) {
            return byReviewTime;
        }
        int byWrongTime = compareNullableDate(left.getWrongTime(), right.getWrongTime());
        if (byWrongTime != 0) {
            return byWrongTime;
        }
        int byUpdateTime = compareNullableDate(left.getUpdateTime(), right.getUpdateTime());
        if (byUpdateTime != 0) {
            return byUpdateTime;
        }
        return compareNullableDate(left.getStudyTime(), right.getStudyTime());
    }

    private int compareNullableDate(Date left, Date right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private <T> List<T> paginateList(List<T> source, long current, long pageSize) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        long start = (current - 1) * pageSize;
        if (start >= source.size()) {
            return Collections.emptyList();
        }
        long end = Math.min(start + pageSize, source.size());
        return source.subList((int) start, (int) end);
    }

    private WordContext buildWordContext(long userId, Set<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            return new WordContext(Collections.emptyMap(), Collections.emptyMap());
        }
        Map<Long, DailyWord> dailyWordMap = dailyWordService.listByIds(wordIds).stream()
                .collect(Collectors.toMap(DailyWord::getId, Function.identity(), (left, right) -> left));
        Map<Long, UserWordBook> userWordBookMap = userWordBookService.list(new QueryWrapper<UserWordBook>()
                        .eq("user_id", userId)
                        .eq("is_deleted", 0)
                        .in("word_id", wordIds))
                .stream()
                .collect(Collectors.toMap(UserWordBook::getWordId, Function.identity(), (left, right) -> left));
        return new WordContext(dailyWordMap, userWordBookMap);
    }

    private DailyWordStudyRecordVO toStudyRecordVO(UserDailyWord userDailyWord,
                                                   DailyWord dailyWord,
                                                   UserWordBook userWordBook) {
        DailyWordStudyRecordVO vo = new DailyWordStudyRecordVO();
        vo.setWordId(userDailyWord.getWordId());
        vo.setIsStudied(defaultZero(userDailyWord.getIsStudied()));
        vo.setMasteryLevel(userDailyWord.getMasteryLevel());
        vo.setStudyTime(userDailyWord.getStudyTime());
        vo.setUpdateTime(userDailyWord.getUpdateTime());
        vo.setNote(userDailyWord.getNoteContent());
        vo.setIsLiked(defaultZero(userDailyWord.getIsLiked()));
        vo.setIsCollected(userWordBook != null ? defaultZero(userWordBook.getIsCollected()) : 0);
        if (dailyWord != null) {
            vo.setWord(dailyWord.getWord());
            vo.setPhonetic(dailyWord.getPronunciation());
            vo.setTranslation(dailyWord.getTranslation());
        }
        return vo;
    }

    private DailyWordReviewItemVO toReviewItemVO(UserDailyWord userDailyWord,
                                                 DailyWord dailyWord,
                                                 UserWordBook userWordBook) {
        DailyWordReviewItemVO vo = new DailyWordReviewItemVO();
        vo.setWordId(userDailyWord.getWordId());
        vo.setMasteryLevel(userDailyWord.getMasteryLevel());
        vo.setIsCollected(userWordBook != null ? defaultZero(userWordBook.getIsCollected()) : 0);
        vo.setIsWrong(defaultZero(userDailyWord.getIsWrong()));
        vo.setLastStudyTime(userDailyWord.getStudyTime());
        vo.setReviewTime(userDailyWord.getReviewTime());
        vo.setUpdateTime(userDailyWord.getUpdateTime());
        vo.setNote(userDailyWord.getNoteContent());
        if (dailyWord != null) {
            vo.setWord(dailyWord.getWord());
            vo.setPhonetic(dailyWord.getPronunciation());
            vo.setTranslation(dailyWord.getTranslation());
        }
        return vo;
    }

    private UserDailyWord getOrCreateUserDailyWord(long userId, long wordId, boolean requireWordExists) {
        if (requireWordExists) {
            validateWordExists(wordId);
        }
        UserDailyWord userDailyWord = getUserDailyWord(wordId, userId);
        if (userDailyWord != null) {
            return userDailyWord;
        }
        UserDailyWord newRecord = new UserDailyWord();
        newRecord.setUserId(userId);
        newRecord.setWordId(wordId);
        newRecord.setIsStudied(0);
        newRecord.setIsLiked(0);
        newRecord.setIsWrong(0);
        newRecord.setCreateTime(new Date());
        newRecord.setUpdateTime(new Date());
        return newRecord;
    }

    private boolean saveOrUpdateUserDailyWord(UserDailyWord userDailyWord) {
        if (userDailyWord.getId() == null) {
            if (userDailyWord.getCreateTime() == null) {
                userDailyWord.setCreateTime(new Date());
            }
            if (userDailyWord.getUpdateTime() == null) {
                userDailyWord.setUpdateTime(new Date());
            }
            return this.save(userDailyWord);
        }
        return this.updateById(userDailyWord);
    }

    private void validateMasteryLevel(int masteryLevel) {
        if (masteryLevel < 1 || masteryLevel > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "mastery_level is invalid");
        }
    }

    private void validateWordExists(long wordId) {
        DailyWord dailyWord = dailyWordService.getById(wordId);
        if (dailyWord == null || (dailyWord.getIsDelete() != null && dailyWord.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "daily word not found");
        }
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static final class WordContext {
        private final Map<Long, DailyWord> dailyWordMap;
        private final Map<Long, UserWordBook> userWordBookMap;

        private WordContext(Map<Long, DailyWord> dailyWordMap, Map<Long, UserWordBook> userWordBookMap) {
            this.dailyWordMap = dailyWordMap;
            this.userWordBookMap = userWordBookMap;
        }
    }
}
