package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.model.dto.userwordbook.UserWordBookAddRequest;
import com.ttbt.smartclass.model.dto.userwordbook.UserWordBookQueryRequest;
import com.ttbt.smartclass.model.dto.userwordbook.UserWordBookUpdateDifficultyRequest;
import com.ttbt.smartclass.model.dto.userwordbook.UserWordBookUpdateStatusRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.entity.UserWordBook;
import com.ttbt.smartclass.model.vo.UserWordBookVO;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.UserWordBookService;
import java.util.List;
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
 * 用户单词本控制器。
 * 负责单词本添加、移除、学习状态、难度、收藏和分页查询接口。
 */
@RestController
@RequestMapping("/word-books")
@Slf4j
public class UserWordBookController {

    @Resource
    private UserWordBookService userWordBookService;

    @Resource
    private UserService userService;

    /**
     * 将单词加入当前用户的单词本。
     *
     * @param addRequest 单词本新增请求
     * @param request 当前 HTTP 请求
     * @return 是否添加成功
     */
    @PostMapping("")
    public BaseResponse<Boolean> addToWordBook(@RequestBody UserWordBookAddRequest addRequest,
                                               HttpServletRequest request) {
        // 校验单词 id，添加单词本必须指定目标单词
        if (addRequest == null || addRequest.getWordId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "word_id is required");
        }
        // 获取当前登录用户，并将单词加入该用户自己的单词本
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.addToWordBook(loginUser.getId(), addRequest.getWordId(), addRequest.getDifficulty());
        return ResultUtils.success(result);
    }

    /**
     * 从当前用户单词本中移除指定单词。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否移除成功
     */
    @DeleteMapping("/{word_id}")
    public BaseResponse<Boolean> removeFromWordBook(@PathVariable("word_id") Long wordId,
                                                    HttpServletRequest request) {
        // 校验单词 id，移除操作需要明确目标单词
        if (wordId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "word_id is required");
        }
        // 获取当前登录用户，并逻辑删除该用户单词本中的记录
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.removeFromWordBook(loginUser.getId(), wordId);
        return ResultUtils.success(result);
    }

    /**
     * 更新当前用户单词本中单词的学习状态。
     *
     * @param wordId 单词 ID
     * @param updateStatusRequest 学习状态更新请求
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PutMapping("/{word_id}/status")
    public BaseResponse<Boolean> updateLearningStatus(@PathVariable("word_id") Long wordId,
                                                      @RequestBody UserWordBookUpdateStatusRequest updateStatusRequest,
                                                      HttpServletRequest request) {
        // 校验学习状态参数，状态缺失时不允许更新
        if (updateStatusRequest == null || updateStatusRequest.getLearningStatus() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "learning_status is required");
        }
        // 获取当前登录用户，只更新自己的单词本记录
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.updateLearningStatus(loginUser.getId(), wordId, updateStatusRequest.getLearningStatus());
        return ResultUtils.success(result);
    }

    /**
     * 更新当前用户单词本中单词的难度。
     *
     * @param wordId 单词 ID
     * @param difficultyRequest 难度更新请求
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PutMapping("/{word_id}/difficulty")
    public BaseResponse<Boolean> updateDifficulty(@PathVariable("word_id") Long wordId,
                                                  @RequestBody UserWordBookUpdateDifficultyRequest difficultyRequest,
                                                  HttpServletRequest request) {
        // 校验难度参数，难度缺失时不允许更新
        if (difficultyRequest == null || difficultyRequest.getDifficulty() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "difficulty is required");
        }
        // 获取当前登录用户，只更新自己的单词本难度
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.updateDifficulty(loginUser.getId(), wordId, difficultyRequest.getDifficulty());
        return ResultUtils.success(result);
    }

    /**
     * 将指定单词标记为当前用户重点关注单词。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @PutMapping("/{word_id}/focus")
    public BaseResponse<Boolean> markFocusedWord(@PathVariable("word_id") Long wordId,
                                                 HttpServletRequest request) {
        // 校验单词 id，重点关注必须定位到具体单词
        if (wordId == null || wordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "word_id is invalid");
        }
        // 获取当前登录用户，并将单词加入或恢复到单词本收藏状态
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.markWordAsCollected(loginUser.getId(), wordId);
        return ResultUtils.success(result);
    }

    /**
     * 取消当前用户对指定单词的重点关注。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否取消成功
     */
    @DeleteMapping("/{word_id}/focus")
    public BaseResponse<Boolean> cancelFocusedWord(@PathVariable("word_id") Long wordId,
                                                   HttpServletRequest request) {
        // 校验单词 id，取消关注必须定位到具体单词
        if (wordId == null || wordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "word_id is invalid");
        }
        // 获取当前登录用户，并清除自己的单词收藏标记
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.cancelWordCollected(loginUser.getId(), wordId);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询当前用户的单词本。
     *
     * @param userWordBookQueryRequest 单词本查询条件
     * @param request 当前 HTTP 请求
     * @return 单词本分页数据
     */
    @GetMapping("/page")
    public BaseResponse<Page<UserWordBookVO>> listUserWordBookByPage(UserWordBookQueryRequest userWordBookQueryRequest,
                                                                     HttpServletRequest request) {
        // 校验查询请求，并强制绑定当前登录用户，避免查询他人单词本
        if (userWordBookQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "query request is required");
        }
        User loginUser = userService.getLoginUser(request);
        userWordBookQueryRequest.setUserId(loginUser.getId());

        long current = userWordBookQueryRequest.getCurrent();
        long size = userWordBookQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 根据筛选条件分页查询单词本记录，并转换为包含单词详情的 VO
        QueryWrapper<UserWordBook> queryWrapper = userWordBookService.getQueryWrapper(userWordBookQueryRequest);
        Page<UserWordBook> userWordBookPage = userWordBookService.page(new Page<>(current, size), queryWrapper);
        Page<UserWordBookVO> userWordBookVOPage = new Page<>(current, size, userWordBookPage.getTotal());
        List<UserWordBookVO> userWordBookVOList = userWordBookService.getUserWordBookVO(userWordBookPage.getRecords());
        userWordBookVOPage.setRecords(userWordBookVOList);
        return ResultUtils.success(userWordBookVOPage);
    }

    @GetMapping("/statistics")
    public BaseResponse<int[]> getUserWordBookStatistics(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int[] statistics = userWordBookService.getUserWordBookStatistics(loginUser.getId());
        return ResultUtils.success(statistics);
    }

    /**
     * 判断指定单词是否已在当前用户单词本中。
     *
     * @param wordId 单词 ID
     * @param request 当前 HTTP 请求
     * @return 是否已加入单词本
     */
    @GetMapping("/{word_id}/exists")
    public BaseResponse<Boolean> isWordInUserBook(@PathVariable("word_id") Long wordId,
                                                  HttpServletRequest request) {
        // 校验单词 id，避免无效查询
        if (wordId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "word_id is required");
        }
        // 获取当前登录用户，并判断该用户是否存在未删除的单词本记录
        User loginUser = userService.getLoginUser(request);
        boolean result = userWordBookService.isWordInUserBook(loginUser.getId(), wordId);
        return ResultUtils.success(result);
    }

    @GetMapping("")
    public BaseResponse<List<UserWordBookVO>> getUserWordBookList(@RequestParam(required = false) Integer learningStatus,
                                                                  @RequestParam(required = false) Integer isCollected,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<UserWordBookVO> userWordBookVOList = userWordBookService.getUserWordBookList(loginUser.getId(), learningStatus, isCollected);
        return ResultUtils.success(userWordBookVOList);
    }
}
