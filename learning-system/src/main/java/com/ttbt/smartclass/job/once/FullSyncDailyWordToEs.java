package com.ttbt.smartclass.job.once;

import cn.hutool.core.collection.CollUtil;
import com.ttbt.smartclass.esdao.DailyWordEsDao;
import com.ttbt.smartclass.model.dto.dailyword.DailyWordEsDTO;
import com.ttbt.smartclass.model.entity.DailyWord;
import com.ttbt.smartclass.service.DailyWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步每日单词到 ES
 */
@Component
@Slf4j
public class FullSyncDailyWordToEs implements CommandLineRunner {

    @Resource
    private DailyWordService dailyWordService;

    @Resource
    private DailyWordEsDao dailyWordEsDao;

    /**
     * 启动时全量同步每日单词数据到 ES。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        try {
            // 从数据库读取全部每日单词，没有数据时跳过同步
            List<DailyWord> dailyWordList = dailyWordService.list();
            if (CollUtil.isEmpty(dailyWordList)) {
                return;
            }
            // 将数据库实体转换为 ES 文档 DTO
            List<DailyWordEsDTO> dailyWordEsDTOList = dailyWordList.stream()
                    .map(DailyWordEsDTO::objToDto)
                    .collect(Collectors.toList());
            final int pageSize = 500;
            int total = dailyWordEsDTOList.size();
            log.info("FullSyncDailyWordToEs start, total {}", total);
            // 按 500 条分批写入 ES，减少单次同步压力
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                log.info("sync from {} to {}", i, end);
                dailyWordEsDao.saveAll(dailyWordEsDTOList.subList(i, end));
            }
            log.info("FullSyncDailyWordToEs end, total {}", total);
        } catch (Exception e) {
            log.error("FullSyncDailyWordToEs failed, skip startup sync", e);
        }
    }
} 
