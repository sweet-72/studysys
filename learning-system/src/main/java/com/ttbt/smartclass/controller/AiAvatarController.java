package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.dto.aiavatar.AiAvatarAddRequest;
import com.ttbt.smartclass.model.dto.aiavatar.AiAvatarQueryRequest;
import com.ttbt.smartclass.model.dto.aiavatar.AiAvatarUpdateRequest;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.AiAvatarVO;
import com.ttbt.smartclass.service.AiAvatarService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI分身接口
 */
@RestController
@RequestMapping("/ai-avatars")
@Slf4j
public class AiAvatarController {

    @Resource
    private AiAvatarService aiAvatarService;

    @Resource
    private UserService userService;

    /**
     * 创建AI分身
     *
     * @param aiAvatarAddRequest 添加请求
     * @param request 请求体
     * @return baseResponse
     */
    @PostMapping("")
    public BaseResponse<Long> addAiAvatar(@RequestBody AiAvatarAddRequest aiAvatarAddRequest, HttpServletRequest request) {
        if (aiAvatarAddRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        AiAvatar aiAvatar = new AiAvatar();
        BeanUtils.copyProperties(aiAvatarAddRequest, aiAvatar);
        // 校验
        User loginUser = userService.getLoginUser(request);
        aiAvatar.setCreatorId(loginUser.getId());
        boolean result = aiAvatarService.save(aiAvatar);
        if (!result) {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(aiAvatar.getId());
    }

    /**
     * 删除AI分身
     *
     * @param id 要删除的资源ID
     * @param request 请求体
     * @return baseResponse
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteAiAvatar(@PathVariable("id") Long id, HttpServletRequest request) {
        if (id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        AiAvatar oldAiAvatar = aiAvatarService.getById(id);
        if (oldAiAvatar == null) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldAiAvatar.getCreatorId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = aiAvatarService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新AI分身
     *
     * @param id 要更新的资源ID
     * @param aiAvatarUpdateRequest ai更新请求
     * @param request 请求体
     * @return baseResponse
     */
    @PutMapping("/{id}")
    public BaseResponse<Boolean> updateAiAvatar(@PathVariable("id") Long id, 
            @RequestBody AiAvatarUpdateRequest aiAvatarUpdateRequest,
            HttpServletRequest request) {
        if (aiAvatarUpdateRequest == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        
        // 设置ID
        aiAvatarUpdateRequest.setId(id);
        
        // 参数校验
        User loginUser = userService.getLoginUser(request);
        
        // 判断是否存在
        AiAvatar oldAiAvatar = aiAvatarService.getById(id);
        if (oldAiAvatar == null) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        
        // 仅本人或管理员可修改
        if (!oldAiAvatar.getCreatorId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 交给service层处理增量更新字段转换
        AiAvatar aiAvatar = aiAvatarService.createUpdateEntity(aiAvatarUpdateRequest);
        
        boolean result = aiAvatarService.updateById(aiAvatar);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新AI分身
     *
     * @param id 要更新的资源ID
     * @param aiAvatar ai分身完整实体
     * @param request 请求体
     * @return baseResponse
     */
    @PutMapping("/{id}/admin")
    public BaseResponse<Boolean> updateAiAvatarAdmin(@PathVariable("id") Long id, 
            @RequestBody AiAvatar aiAvatar,
            HttpServletRequest request) {
        if (aiAvatar == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        
        // 设置ID
        aiAvatar.setId(id);
        
        // 参数校验
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 判断是否存在
        AiAvatar oldAiAvatar = aiAvatarService.getById(id);
        if (oldAiAvatar == null) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        
        boolean result = aiAvatarService.updateById(aiAvatar);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取AI分身
     *
     * @param id ai分身id
     * @return baseResponse
     */
    @GetMapping("/{id}")
    public BaseResponse<AiAvatarVO> getAiAvatarById(@PathVariable("id") Long id,
                                                    HttpServletRequest request) {
        if (id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        AiAvatar aiAvatar = aiAvatarService.getById(id);
        if (aiAvatar == null || isDeleted(aiAvatar)) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUserPermitNull(request);
        if (!canViewInUserCenter(aiAvatar, loginUser)) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "AI avatar not found");
        }
        AiAvatarVO aiAvatarVO = new AiAvatarVO();
        BeanUtils.copyProperties(aiAvatar, aiAvatarVO);
        return ResultUtils.success(aiAvatarVO);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param aiAvatarQueryRequest ai查询请求
     * @return baseResponse
     */
    @GetMapping("/admin")
    public BaseResponse<List<AiAvatar>> listAiAvatar(AiAvatarQueryRequest aiAvatarQueryRequest) {
        if (aiAvatarQueryRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        AiAvatar aiAvatarQuery = new AiAvatar();
        BeanUtils.copyProperties(aiAvatarQueryRequest, aiAvatarQuery);
        
        QueryWrapper<AiAvatar> queryWrapper = new QueryWrapper<>(aiAvatarQuery);
        List<AiAvatar> aiAvatarList = aiAvatarService.list(queryWrapper);
        return ResultUtils.success(aiAvatarList);
    }

    /**
     * 分页获取列表
     *
     * @param aiAvatarQueryRequest ai查询请求
     * @return baseResponse
     */
    @GetMapping("/page")
    public BaseResponse<Page<AiAvatarVO>> listAiAvatarByPage(AiAvatarQueryRequest aiAvatarQueryRequest) {
        // 校验分页查询请求，避免空对象进入后续筛选逻辑
        if (aiAvatarQueryRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long current = aiAvatarQueryRequest.getCurrent();
        long size = aiAvatarQueryRequest.getPageSize();
        
        // 限制单次分页数量，避免用户端一次拉取过多 AI 分身数据
        if (size > 50) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        // 用户端只展示未删除、公开且启用的 AI 分身
        QueryWrapper<AiAvatar> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0)
                .eq("is_public", 1)
                .eq("status", 1);

        // 关键字同时匹配名称、简介和标签，覆盖用户常用搜索入口
        if (StringUtils.isNotBlank(aiAvatarQueryRequest.getKeyword())) {
            String keyword = aiAvatarQueryRequest.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like("name", keyword)
                    .or()
                    .like("description", keyword)
                    .or()
                    .like("tags", keyword));
        }

        // 按配置排序、使用次数、评分和创建时间排序，优先展示更推荐的 AI 分身
        queryWrapper.orderByAsc("sort")
                .orderByDesc("usage_count")
                .orderByDesc("rating")
                .orderByDesc("create_time");

        Page<AiAvatar> aiAvatarPage = aiAvatarService.page(new Page<>(current, size), queryWrapper);

        // 将实体分页转换为前端展示用 VO，避免直接暴露数据库实体
        Page<AiAvatarVO> aiAvatarVOPage = new Page<>(current, size, aiAvatarPage.getTotal());
        List<AiAvatarVO> aiAvatarVOList = aiAvatarPage.getRecords().stream().map(aiAvatar -> {
            AiAvatarVO aiAvatarVO = new AiAvatarVO();
            BeanUtils.copyProperties(aiAvatar, aiAvatarVO);
            return aiAvatarVO;
        }).collect(Collectors.toList());
        aiAvatarVOPage.setRecords(aiAvatarVOList);
        return ResultUtils.success(aiAvatarVOPage);
    }
    
    /**
     * 管理员分页获取列表
     *
     * @param aiAvatarQueryRequest ai查询请求
     * @return baseResponse
     */
    @GetMapping("/admin/page")
    public BaseResponse<Page<AiAvatar>> listAiAvatarByPageAdmin(AiAvatarQueryRequest aiAvatarQueryRequest) {
        if (aiAvatarQueryRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        AiAvatar aiAvatarQuery = new AiAvatar();
        BeanUtils.copyProperties(aiAvatarQueryRequest, aiAvatarQuery);
        long current = aiAvatarQueryRequest.getCurrent();
        long size = aiAvatarQueryRequest.getPageSize();
        String sortField = aiAvatarQueryRequest.getSortField();
        String sortOrder = aiAvatarQueryRequest.getSortOrder();
        
        // 限制爬虫
        if (size > 50) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        
        QueryWrapper<AiAvatar> queryWrapper = new QueryWrapper<>(aiAvatarQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        Page<AiAvatar> aiAvatarPage = aiAvatarService.page(new Page<>(current, size), queryWrapper);
        
        return ResultUtils.success(aiAvatarPage);
    }

    private boolean canViewInUserCenter(AiAvatar aiAvatar, User loginUser) {
        if (aiAvatar == null) {
            return false;
        }
        if (loginUser != null && userService.isAdmin(loginUser)) {
            return true;
        }
        return !isDeleted(aiAvatar)
                && Integer.valueOf(1).equals(aiAvatar.getIsPublic())
                && Integer.valueOf(1).equals(aiAvatar.getStatus());
    }

    private boolean isDeleted(AiAvatar aiAvatar) {
        return aiAvatar != null && aiAvatar.getIsDelete() != null && aiAvatar.getIsDelete() != 0;
    }
}
