package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordReviewUpdateRequest;
import com.ttbt.smartclass.model.entity.DailyWord;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.entity.UserDailyWord;
import com.ttbt.smartclass.model.vo.DailyWordReviewItemVO;
import com.ttbt.smartclass.model.vo.DailyWordStudyRecordVO;
import com.ttbt.smartclass.service.DailyWordService;
import com.ttbt.smartclass.service.UserDailyWordService;
import com.ttbt.smartclass.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日单词学习控制器。
 * 负责用户单词学习状态、掌握度、错词和复习结果等学习行为接口。
 */
@RestController
@RequestMapping("/daily/word/learning")
@Slf4j
public class DailyWordLearningController {

    @Resource
    private UserDailyWordService userDailyWordService;

    @Resource
    private DailyWordService dailyWordService;

    @Resource
    private UserService userService;

    /**
     * 标记当前用户已学习指定每日单词。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @PostMapping("/{word_id}/study-status")
    public BaseResponse<Boolean> markWordAsStudied(@PathVariable("word_id") long wordId,
                                                   HttpServletRequest request) {
        // 校验单词存在且未删除，避免写入无效学习记录
        DailyWord dailyWord = validateWord(wordId);
        // 获取当前登录用户，只更新自己的学习状态
        User loginUser = userService.getLoginUser(request);
        boolean result = userDailyWordService.markWordAsStudied(dailyWord.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 取消当前用户指定每日单词的已学状态。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否取消成功
     */
    @DeleteMapping("/{word_id}/study-status")
    public BaseResponse<Boolean> cancelWordStudied(@PathVariable("word_id") long wordId,
                                                   HttpServletRequest request) {
        // 校验单词是否可用，防止对不存在的单词操作学习状态
        DailyWord dailyWord = validateWord(wordId);
        // 获取当前登录用户，并清除该用户自己的已学标记
        User loginUser = userService.getLoginUser(request);
        boolean result = userDailyWordService.cancelWordStudied(dailyWord.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新当前用户对指定每日单词的掌握度。
     *
     * @param wordId 单词 ID
     * @param masteryLevel 掌握度等级
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/{word_id}/mastery")
    public BaseResponse<Boolean> updateMasteryLevel(@PathVariable("word_id") long wordId,
                                                    @RequestParam("mastery_level") int masteryLevel,
                                                    HttpServletRequest request) {
        // 校验单词有效性和掌握度范围，掌握度只允许 1 到 5
        DailyWord dailyWord = validateWord(wordId);
        if (masteryLevel < 1 || masteryLevel > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "mastery_level is invalid");
        }
        // 获取当前登录用户，并写入该用户的掌握度记录
        User loginUser = userService.getLoginUser(request);
        boolean result = userDailyWordService.updateMasteryLevel(dailyWord.getId(), loginUser.getId(), masteryLevel);
        return ResultUtils.success(result);
    }

    /**
     * 保存当前用户对指定每日单词的学习笔记。
     *
     * @param wordId 单词 ID
     * @param noteContent 笔记内容
     * @param request 当前 HTTP 请求
     * @return 是否保存成功
     */
    @PostMapping("/{word_id}/note")
    public BaseResponse<Boolean> saveWordNote(@PathVariable("word_id") long wordId,
                                              @RequestParam("note_content") String noteContent,
                                              HttpServletRequest request) {
        // 校验单词存在且笔记内容不为 null，允许保存空字符串但不允许缺参
        DailyWord dailyWord = validateWord(wordId);
        if (noteContent == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "note_content cannot be null");
        }
        // 获取当前登录用户，并保存该用户自己的单词笔记
        User loginUser = userService.getLoginUser(request);
        boolean result = userDailyWordService.saveWordNote(dailyWord.getId(), loginUser.getId(), noteContent);
        return ResultUtils.success(result);
    }

    @GetMapping("/{word_id}")
    public BaseResponse<UserDailyWord> getUserDailyWord(@PathVariable("word_id") long wordId,
                                                        HttpServletRequest request) {
        DailyWord dailyWord = validateWord(wordId);
        User loginUser = userService.getLoginUser(request);
        UserDailyWord userDailyWord = userDailyWordService.getUserDailyWord(dailyWord.getId(), loginUser.getId());
        return ResultUtils.success(userDailyWord);
    }

    /**
     * 分页查询当前用户的每日单词学习记录。
     *
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 学习记录分页数据
     */
    @GetMapping("/records/page")
    public BaseResponse<Page<DailyWordStudyRecordVO>> listMyStudyRecords(@RequestParam(defaultValue = "1") long current,
                                                                          @RequestParam(defaultValue = "10") long pageSize,
                                                                          HttpServletRequest request) {
        // 限制分页参数和单页数量，避免一次性查询过多学习记录
        validatePage(current, pageSize, 50);
        // 获取当前登录用户，只查询自己的每日单词学习记录
        User loginUser = userService.getLoginUser(request);
        Page<DailyWordStudyRecordVO> resultPage = userDailyWordService.listStudyRecordPage(loginUser.getId(), current, pageSize);
        return ResultUtils.success(resultPage);
    }

    /**
     * 将指定每日单词标记为当前用户的错词。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @PutMapping("/{word_id}/wrong-status")
    public BaseResponse<Boolean> markWordAsWrong(@PathVariable("word_id") long wordId,
                                                 HttpServletRequest request) {
        // 校验单词有效性，确保错词记录关联到真实单词
        DailyWord dailyWord = validateWord(wordId);
        // 获取当前登录用户，并写入该用户的错词状态
        User loginUser = userService.getLoginUser(request);
        boolean result = userDailyWordService.markWordAsWrong(dailyWord.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 取消当前用户指定每日单词的错词标记。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否取消成功
     */
    @DeleteMapping("/{word_id}/wrong-status")
    public BaseResponse<Boolean> cancelWordWrong(@PathVariable("word_id") long wordId,
                                                 HttpServletRequest request) {
        // 校验单词有效性，避免清理无效错词记录
        DailyWord dailyWord = validateWord(wordId);
        // 获取当前登录用户，并清除该用户自己的错词状态
        User loginUser = userService.getLoginUser(request);
        boolean result = userDailyWordService.cancelWordWrong(dailyWord.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 分页查询当前用户的错词列表。
     *
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 错词分页数据
     */
    @GetMapping("/wrong-words/page")
    public BaseResponse<Page<DailyWordReviewItemVO>> listMyWrongWords(@RequestParam(defaultValue = "1") long current,
                                                                      @RequestParam(defaultValue = "10") long pageSize,
                                                                      HttpServletRequest request) {
        // 校验分页参数，错词列表单页最多返回 50 条
        validatePage(current, pageSize, 50);
        // 获取当前登录用户，只查询自己的错词列表
        User loginUser = userService.getLoginUser(request);
        Page<DailyWordReviewItemVO> resultPage = userDailyWordService.listWrongWordPage(loginUser.getId(), current, pageSize);
        return ResultUtils.success(resultPage);
    }

    /**
     * 分页查询当前用户需要复习的每日单词。
     *
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 复习单词分页数据
     */
    @GetMapping("/review-words/page")
    public BaseResponse<Page<DailyWordReviewItemVO>> listMyReviewWords(@RequestParam(defaultValue = "1") long current,
                                                                       @RequestParam(defaultValue = "10") long pageSize,
                                                                       HttpServletRequest request) {
        // 校验分页参数，复习列表单页最多返回 50 条
        validatePage(current, pageSize, 50);
        // 获取当前登录用户，并查询错词、低掌握度和收藏单词合并后的复习列表
        User loginUser = userService.getLoginUser(request);
        Page<DailyWordReviewItemVO> resultPage = userDailyWordService.listReviewWordPage(loginUser.getId(), current, pageSize);
        return ResultUtils.success(resultPage);
    }

    /**
     * 更新当前用户的单词复习结果。
     *
     * @param wordId 单词 ID
     * @param reviewUpdateRequest 复习结果请求
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/{word_id}/review")
    public BaseResponse<Boolean> updateReviewResult(@PathVariable("word_id") long wordId,
                                                    @RequestBody(required = false) DailyWordReviewUpdateRequest reviewUpdateRequest,
                                                    HttpServletRequest request) {
        // 校验单词是否存在且未删除，避免为无效单词写入复习记录
        DailyWord dailyWord = validateWord(wordId);
        // 获取当前登录用户，复习结果只写入当前用户自己的学习记录
        User loginUser = userService.getLoginUser(request);
        // 更新复习时间、掌握度和错词状态等学习结果
        boolean result = userDailyWordService.updateReviewResult(dailyWord.getId(), loginUser.getId(), reviewUpdateRequest);
        return ResultUtils.success(result);
    }

    private DailyWord validateWord(long wordId) {
        if (wordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "word_id is invalid");
        }
        DailyWord dailyWord = dailyWordService.getById(wordId);
        if (dailyWord == null || (dailyWord.getIsDelete() != null && dailyWord.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "daily word not found");
        }
        return dailyWord;
    }

    private void validatePage(long current, long pageSize, long maxPageSize) {
        if (current <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "current or pageSize is invalid");
        }
        if (pageSize > maxPageSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize must not exceed " + maxPageSize);
        }
    }
}
