package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.section.SectionAddRequest;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.SectionVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 小节服务接口
 */
public interface SectionService extends IService<Section> {

    /**
     * 创建小节（管理员）
     * 
     * @param sectionAddRequest 小节添加请求
     * @param loginUser 登录用户
     * @return 小节 ID
     */
    Long createSection(SectionAddRequest sectionAddRequest, User loginUser);

    /**
     * 获取小节详情（用户端）
     * 
     * @param sectionId 小节 ID
     * @return 小节 VO
     */
    SectionVO getSectionDetail(Long sectionId);
}
