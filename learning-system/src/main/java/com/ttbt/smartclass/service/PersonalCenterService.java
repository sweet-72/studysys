package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.model.vo.PersonalCenterOverviewVO;
import com.ttbt.smartclass.model.vo.PersonalFavouriteCourseVO;
import com.ttbt.smartclass.model.vo.PersonalLearningRecordVO;
import com.ttbt.smartclass.model.vo.LearningHistoryItemVO;

/**
 * 个人中心聚合服务。
 */
public interface PersonalCenterService {

    /**
     * Build overview data for current login user.
     *
     * @param userId current login user id
     * @return personal center overview
     */
    PersonalCenterOverviewVO getOverview(Long userId);

    /**
     * Page favourite course details for current login user.
     *
     * @param userId current login user id
     * @param current current page
     * @param pageSize page size
     * @return favourite course detail page
     */
    Page<PersonalFavouriteCourseVO> getFavouriteCoursePage(Long userId, long current, long pageSize);

    /**
     * Page recent learning records for current login user.
     *
     * @param userId current login user id
     * @param current current page
     * @param pageSize page size
     * @return personal learning record page
     */
    Page<PersonalLearningRecordVO> getLearningRecordPage(Long userId, long current, long pageSize);

    Page<LearningHistoryItemVO> getLearningHistoryPage(Long userId, long current, long pageSize);
}
