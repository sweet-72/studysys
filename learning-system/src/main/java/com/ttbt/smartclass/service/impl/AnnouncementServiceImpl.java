package com.ttbt.smartclass.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.CommonConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.AnnouncementMapper;
import com.ttbt.smartclass.mapper.UserAnnouncementReaderMapper;
import com.ttbt.smartclass.model.dto.announcement.AnnouncementQueryRequest;
import com.ttbt.smartclass.model.entity.Announcement;
import com.ttbt.smartclass.model.entity.UserAnnouncementReader;
import com.ttbt.smartclass.model.vo.AnnouncementVO;
import com.ttbt.smartclass.service.AnnouncementService;
import com.ttbt.smartclass.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Service
@Slf4j
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Resource
    private UserAnnouncementReaderMapper userAnnouncementReaderMapper;

    @Override
    public long addAnnouncement(Announcement announcement, Long adminId) {
        if (announcement == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "params is null");
        }
        if (StringUtils.isBlank(announcement.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告标题不能为空");
        }
        if (StringUtils.isBlank(announcement.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "content must not be blank");
        }
        if (announcement.getPriority() == null) {
            announcement.setPriority(0); // 默认优先级为普通
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(0); // 默认状态为草稿
        }
        announcement.setAdminId(adminId);
        announcement.setViewCount(0); // 新公告浏览量默认为 0
        boolean result = this.save(announcement);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "add announcement failed");
        }
        return announcement.getId();
    }

    @Override
    public QueryWrapper<Announcement> getQueryWrapper(AnnouncementQueryRequest announcementQueryRequest) {
        if (announcementQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "query request is null");
        }
        Long id = announcementQueryRequest.getId();
        String title = announcementQueryRequest.getTitle();
        String content = announcementQueryRequest.getContent();
        Integer priority = announcementQueryRequest.getPriority();
        Integer status = announcementQueryRequest.getStatus();
        Date startTime = announcementQueryRequest.getStartTime();
        Date endTime = announcementQueryRequest.getEndTime();
        String coverImage = announcementQueryRequest.getCoverImage();
        Long adminId = announcementQueryRequest.getAdminId();
        Date createTime = announcementQueryRequest.getCreateTime();
        Boolean isValid = announcementQueryRequest.getIsValid();
        String sortField = announcementQueryRequest.getSortField();
        String sortOrder = announcementQueryRequest.getSortOrder();

        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.eq(priority != null, "priority", priority);
        queryWrapper.eq(status != null && !Boolean.TRUE.equals(isValid), "status", status);
        queryWrapper.ge(startTime != null && !Boolean.TRUE.equals(isValid), "start_time", startTime);
        queryWrapper.le(endTime != null && !Boolean.TRUE.equals(isValid), "end_time", endTime);
        queryWrapper.like(StringUtils.isNotBlank(coverImage), "cover_image", coverImage);
        queryWrapper.eq(adminId != null, "admin_id", adminId);
        queryWrapper.eq(createTime != null, "create_time", createTime);
        
        // isValid 为 true 时，只查询当前生效的公告
        if (Boolean.TRUE.equals(isValid)) {
            queryWrapper.eq("status", 1); // 已发布状态
            queryWrapper.le("start_time", new Date()); // 生效时间不晚于当前时间
            queryWrapper.ge("end_time", new Date()); // 结束时间不早于当前时间
            // 生效公告按优先级和创建时间倒序展示
            queryWrapper.orderByDesc("priority", "create_time");
        } else {
            queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                    sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                    SqlUtils.normalizeSortField(sortField));
        }
        
        return queryWrapper;
    }

    @Override
    public AnnouncementVO getAnnouncementVO(Announcement announcement) {
        if (announcement == null) {
            return null;
        }
        AnnouncementVO announcementVO = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, announcementVO);
        return announcementVO;
    }

    @Override
    public List<AnnouncementVO> getAnnouncementVO(List<Announcement> announcementList) {
        if (CollUtil.isEmpty(announcementList)) {
            return new ArrayList<>();
        }
        return announcementList.stream().map(this::getAnnouncementVO).collect(Collectors.toList());
    }

    @Override
    public Page<AnnouncementVO> listValidAnnouncements(long current, long size) {
        // 查询当前生效的公告，按优先级和创建时间排序
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 已发布状态
        queryWrapper.le("start_time", new Date()); // 生效时间不晚于当前时间
        queryWrapper.ge("end_time", new Date()); // 结束时间不早于当前时间
        queryWrapper.orderByDesc("priority", "create_time"); // 优先级高、创建时间新的公告优先展示

        Page<Announcement> announcementPage = this.page(new Page<>(current, size), queryWrapper);
        List<AnnouncementVO> announcementVOList = getAnnouncementVO(announcementPage.getRecords());
        
        Page<AnnouncementVO> announcementVOPage = new Page<>(current, size, announcementPage.getTotal());
        announcementVOPage.setRecords(announcementVOList);
        return announcementVOPage;
    }

    @Override
    public boolean increaseViewCount(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid id");
        }
        UpdateWrapper<Announcement> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.setSql("view_count = view_count + 1");
        return this.update(updateWrapper);
    }

    @Override
    public boolean readAnnouncement(Long announcementId, Long userId) {
        if (announcementId == null || announcementId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid params");
        }

        // 查询公告是否存在
        Announcement announcement = this.getById(announcementId);
        if (announcement == null || announcement.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "announcement not found");
        }

        // 已读则直接返回
        if (hasReadAnnouncement(announcementId, userId)) {
            return true;
        }

        // 记录用户公告已读信息
        UserAnnouncementReader userAnnouncementReader = new UserAnnouncementReader();
        userAnnouncementReader.setUserId(userId);
        userAnnouncementReader.setAnnouncementId(announcementId);
        userAnnouncementReader.setReadTime(new Date());
        userAnnouncementReader.setCreateTime(new Date());
        return userAnnouncementReaderMapper.insert(userAnnouncementReader) > 0;
    }

    @Override
    public boolean hasReadAnnouncement(Long announcementId, Long userId) {
        if (announcementId == null || announcementId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid params");
        }

        QueryWrapper<UserAnnouncementReader> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("announcement_id", announcementId);
        queryWrapper.eq("user_id", userId);
        return userAnnouncementReaderMapper.selectCount(queryWrapper) > 0;
    }
} 
