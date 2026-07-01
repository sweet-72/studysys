package com.ttbt.smartclass.job.once;

import cn.hutool.core.collection.CollUtil;
import com.ttbt.smartclass.esdao.DailyArticleEsDao;
import com.ttbt.smartclass.model.dto.dailyarticle.DailyArticleEsDTO;
import com.ttbt.smartclass.model.entity.DailyArticle;
import com.ttbt.smartclass.service.DailyArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步每日美文到 ES
 */
@Component
@Slf4j
public class FullSyncDailyArticleToEs implements CommandLineRunner {

    @Resource
    private DailyArticleService dailyArticleService;

    @Resource
    private DailyArticleEsDao dailyArticleEsDao;

    /**
     * 启动时全量同步每日美文数据到 ES。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        try {
            // 从数据库读取全部每日美文，没有数据时跳过同步
            List<DailyArticle> dailyArticleList = dailyArticleService.list();
            if (CollUtil.isEmpty(dailyArticleList)) {
                return;
            }
            // 将数据库实体转换为 ES 文档 DTO
            List<DailyArticleEsDTO> dailyArticleEsDTOList = dailyArticleList.stream()
                    .map(DailyArticleEsDTO::objToDto)
                    .collect(Collectors.toList());
            final int pageSize = 500;
            int total = dailyArticleEsDTOList.size();
            log.info("FullSyncDailyArticleToEs start, total {}", total);
            // 按 500 条分批写入 ES，避免一次性提交过多文档
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                log.info("sync from {} to {}", i, end);
                dailyArticleEsDao.saveAll(dailyArticleEsDTOList.subList(i, end));
            }
            log.info("FullSyncDailyArticleToEs end, total {}", total);
        } catch (Exception e) {
            log.error("FullSyncDailyArticleToEs failed, skip startup sync", e);
        }
    }
} 
