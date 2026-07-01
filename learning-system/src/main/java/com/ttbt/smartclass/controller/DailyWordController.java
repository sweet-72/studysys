package com.ttbt.smartclass.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.DeleteRequest;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordAddRequest;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordQueryRequest;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordUpdateRequest;
import com.ttbt.smartclass.model.entity.DailyWord;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.DailyWordVO;
import com.ttbt.smartclass.service.DailyWordService;
import com.ttbt.smartclass.service.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 每日单词接口
 */
@RestController
@RequestMapping("/daily-words")
@Slf4j
public class DailyWordController {

    @Resource
    private DailyWordService dailyWordService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 创建每日单词（仅管理员）
     *
     * @param dailyWordAddRequest 单词创建请求体，包含单词、解释、例句等信息
     * @param request HTTP请求，用于获取当前登录用户
     * @return 新增单词的ID
     */
    @PostMapping("")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDailyWord(@RequestBody DailyWordAddRequest dailyWordAddRequest,
                                         HttpServletRequest request) {
        if (dailyWordAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DailyWord dailyWord = new DailyWord();
        BeanUtils.copyProperties(dailyWordAddRequest, dailyWord);
        User loginUser = userService.getLoginUser(request);
        Long adminId = loginUser.getId();
        // 设置管理员ID
        dailyWord.setAdminId(adminId);
        // 使用saveDailyWord方法，同步到ES
        boolean result = dailyWordService.saveDailyWord(dailyWord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(dailyWord.getId());
    }

    /**
     * 删除每日单词（仅管理员）
     *
     * @param id 要删除单词的ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDailyWord(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        DailyWord oldDailyWord = dailyWordService.getById(id);
        ThrowUtils.throwIf(oldDailyWord == null, ErrorCode.NOT_FOUND_ERROR);
        // 使用deleteDailyWord方法，同步删除ES中的数据
        boolean b = dailyWordService.deleteDailyWord(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新每日单词（仅管理员）
     *
     * @param id 单词ID
     * @param dailyWordUpdateRequest 单词更新请求，包含需要更新的单词信息
     * @return 是否更新成功
     */
    @PutMapping("/{id}/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDailyWord(@PathVariable("id") Long id, 
                                              @RequestBody DailyWordUpdateRequest dailyWordUpdateRequest) {
        if (dailyWordUpdateRequest == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 设置ID
        dailyWordUpdateRequest.setId(id);
        DailyWord dailyWord = new DailyWord();
        BeanUtils.copyProperties(dailyWordUpdateRequest, dailyWord);
        // 判断是否存在
        DailyWord oldDailyWord = dailyWordService.getById(id);
        ThrowUtils.throwIf(oldDailyWord == null, ErrorCode.NOT_FOUND_ERROR);
        // 使用updateDailyWord方法，同步更新ES中的数据
        boolean result = dailyWordService.updateDailyWord(dailyWord);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取每日单词
     *
     * @param id 单词ID
     * @return 单词视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<DailyWordVO> getDailyWordVOById(@PathVariable("id") Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DailyWord dailyWord = dailyWordService.getById(id);
        if (dailyWord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 返回单词详情
        return ResultUtils.success(dailyWordService.getDailyWordVO(dailyWord));
    }

    /**
     * 分页获取单词列表（仅管理员）
     *
     * @param dailyWordQueryRequest 单词查询请求，包含分页参数和查询条件
     * @return 单词分页列表
     */
    @GetMapping("/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DailyWord>> listDailyWordByPage(DailyWordQueryRequest dailyWordQueryRequest) {
        if (dailyWordQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = dailyWordQueryRequest.getCurrent();
        long size = dailyWordQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<DailyWord> queryWrapper = dailyWordService.getQueryWrapper(dailyWordQueryRequest);
        Page<DailyWord> dailyWordPage = dailyWordService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(dailyWordPage);
    }

    /**
     * 分页获取单词列表（封装VO）
     *
     * @param dailyWordQueryRequest 单词查询请求，包含分页参数和查询条件
     * @return 单词视图对象分页列表
     */
    @GetMapping("/page")
    public BaseResponse<Page<DailyWordVO>> listDailyWordVOByPage(DailyWordQueryRequest dailyWordQueryRequest) {
        if (dailyWordQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = dailyWordQueryRequest.getCurrent();
        long size = dailyWordQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<DailyWord> dailyWordPage = dailyWordService.page(new Page<>(current, size),
                dailyWordService.getQueryWrapper(dailyWordQueryRequest));
        Page<DailyWordVO> dailyWordVOPage = new Page<>(current, size, dailyWordPage.getTotal());
        List<DailyWordVO> dailyWordVOList = dailyWordService.getDailyWordVO(dailyWordPage.getRecords());
        dailyWordVOPage.setRecords(dailyWordVOList);
        return ResultUtils.success(dailyWordVOPage);
    }

    /**
     * 获取今日单词
     *
     * @param difficulty 难度等级，可选参数，用于筛选特定难度的单词
     * @return 随机单词
     */
    @GetMapping("/today")
    public BaseResponse<DailyWordVO> getTodayWord(
            @RequestParam(required = false) Integer difficulty) {
        DailyWordVO dailyWordVO = dailyWordService.getRandomDailyWord(difficulty);
        if (dailyWordVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到单词");
        }
        return ResultUtils.success(dailyWordVO);
    }

    /**
     * 从ES搜索单词
     *
     * @param searchText 搜索关键词，服务层会自动匹配单词的所有相关字段
     * @return 符合搜索条件的单词视图对象分页列表
     */
    @GetMapping("/search")
    public BaseResponse<Page<DailyWordVO>> searchDailyWord(@RequestParam String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索关键词不能为空");
        }
        // 调用服务层方法进行搜索，service层负责匹配所有字段
        Page<DailyWord> dailyWordPage = dailyWordService.searchFromEs(searchText);
        Page<DailyWordVO> dailyWordVOPage = new Page<>(dailyWordPage.getCurrent(), 
                dailyWordPage.getSize(), dailyWordPage.getTotal());
        List<DailyWordVO> dailyWordVOList = dailyWordService.getDailyWordVO(dailyWordPage.getRecords());
        dailyWordVOPage.setRecords(dailyWordVOList);
        return ResultUtils.success(dailyWordVOPage);
    }

    /**
     * 导入单词Excel（仅管理员）
     *
     * @param file Excel文件
     * @param request HTTP请求
     * @return 导入成功的数量
     */
    @PostMapping("/import")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> importWords(@RequestParam("file") MultipartFile file,
                                             HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        User loginUser = userService.getLoginUser(request);
        Long adminId = loginUser.getId();

        List<DailyWordImportDTO> importList = new ArrayList<>();
        try {
            // 根据文件后缀判断文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
            }
            
            String lowerFileName = fileName.toLowerCase();
            com.alibaba.excel.support.ExcelTypeEnum excelType;
            
            if (lowerFileName.endsWith(".csv")) {
                excelType = com.alibaba.excel.support.ExcelTypeEnum.CSV;
            } else if (lowerFileName.endsWith(".xlsx")) {
                excelType = com.alibaba.excel.support.ExcelTypeEnum.XLSX;
            } else if (lowerFileName.endsWith(".xls")) {
                excelType = com.alibaba.excel.support.ExcelTypeEnum.XLS;
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 .csv、.xls 或 .xlsx 格式的文件");
            }

            EasyExcel.read(file.getInputStream(), DailyWordImportDTO.class, new AnalysisEventListener<DailyWordImportDTO>() {
                @Override
                public void invoke(DailyWordImportDTO data, AnalysisContext context) {
                    importList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("文件读取完成，共 {} 条数据", importList.size());
                }
            }).excelType(excelType).sheet().doRead();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "读取Excel文件失败：" + e.getMessage());
        }

        if (importList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel文件中没有数据");
        }

        int successCount = 0;
        for (DailyWordImportDTO dto : importList) {
            try {
                DailyWord dailyWord = new DailyWord();
                dailyWord.setWord(dto.getWord());
                dailyWord.setPronunciation(dto.getPronunciation());
                dailyWord.setAudioUrl(dto.getAudioUrl());
                dailyWord.setTranslation(dto.getTranslation());
                dailyWord.setExample(dto.getExample());
                dailyWord.setExampleTranslation(dto.getExampleTranslation());
                dailyWord.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
                dailyWord.setCategory(dto.getCategory());
                dailyWord.setNotes(dto.getNotes());
                dailyWord.setPublishDate(dto.getPublishDate());
                dailyWord.setAdminId(adminId);

                boolean result = dailyWordService.saveDailyWord(dailyWord);
                if (result) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("导入单词失败：{}", dto.getWord(), e);
            }
        }

        return ResultUtils.success(successCount);
    }

    /**
     * Excel/CSV导入DTO
     * 注意：列顺序必须与CSV文件中的列顺序一致
     * 列索引从0开始：word(0) → translation(1) → pronunciation(2) → example(3) → exampleTranslation(4) → category(5) → difficulty(6) → audioUrl(7) → notes(8) → publishDate(9)
     */
    @Data
    public static class DailyWordImportDTO {
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 0)
        private String word;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 1)
        private String translation;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 2)
        private String pronunciation;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 3)
        private String example;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 4)
        private String exampleTranslation;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 5)
        private String category;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 6)
        private Integer difficulty;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 7)
        private String audioUrl;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 8)
        private String notes;
        
        @com.alibaba.excel.annotation.ExcelProperty(index = 9)
        private java.util.Date publishDate;
    }
} 