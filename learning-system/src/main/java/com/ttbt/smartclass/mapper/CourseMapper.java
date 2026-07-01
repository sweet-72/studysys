package com.ttbt.smartclass.mapper;

import com.ttbt.smartclass.model.entity.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程 Mapper 接口
 */
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT c.* " +
            "FROM course c " +
            "WHERE c.status = 1 AND c.is_delete = 0 " +
            "ORDER BY c.view_count DESC, c.student_count DESC, c.rating_score DESC, c.rating_count DESC, c.create_time DESC " +
            "LIMIT #{limit}")
    List<Course> selectHotRecommendCourses(@Param("limit") int limit);
}
