package com.ttbt.smartclass.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
*/
public class SqlUtils {

    /**
     * 升序
     */
    public static final String SORT_ORDER_ASC = "asc";

    /**
     * 降序
     */
    public static final String SORT_ORDER_DESC = "desc";

    /**
     * 校验排序字段是否合法，防止 SQL 注入
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }

    /**
     * 将前端传入的 camelCase 排序字段归一化为数据库的 snake_case 列名。
     */
    public static String normalizeSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return sortField;
        }
        String trimmed = StringUtils.trim(sortField);
        if (!validSortField(trimmed)) {
            return trimmed;
        }
        StringBuilder result = new StringBuilder(trimmed.length() + 8);
        for (int i = 0; i < trimmed.length(); i++) {
            char current = trimmed.charAt(i);
            if (Character.isUpperCase(current)) {
                if (result.length() > 0 && result.charAt(result.length() - 1) != '_') {
                    result.append('_');
                }
                result.append(Character.toLowerCase(current));
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }

    /**
     * 设置默认排序
     * 
     * @param queryWrapper 查询条件
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @param <T> 查询实体类型
     */
    public static <T> void setDefaultOrder(QueryWrapper<T> queryWrapper, String sortField, String sortOrder) {
        if (SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, SORT_ORDER_ASC.equalsIgnoreCase(sortOrder), normalizeSortField(sortField));
        }
    }
}
