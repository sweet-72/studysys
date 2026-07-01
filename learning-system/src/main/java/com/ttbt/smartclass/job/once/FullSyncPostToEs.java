package com.ttbt.smartclass.job.once;

import com.ttbt.smartclass.esdao.PostEsDao;
import com.ttbt.smartclass.model.dto.post.PostEsDTO;
import com.ttbt.smartclass.service.PostService;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.ttbt.smartclass.model.entity.Post;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 全量同步帖子到 es
*/
@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 启动时全量同步帖子数据到 ES。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        try {
            // 从数据库读取全部帖子，没有数据时跳过同步
            List<Post> postList = postService.list();
            if (CollUtil.isEmpty(postList)) {
                return;
            }
            // 将帖子实体转换为 ES 文档 DTO
            List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
            final int pageSize = 500;
            int total = postEsDTOList.size();
            log.info("FullSyncPostToEs start, total {}", total);
            // 按 500 条分批写入 ES，避免一次性保存过多帖子文档
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                log.info("sync from {} to {}", i, end);
                postEsDao.saveAll(postEsDTOList.subList(i, end));
            }
            log.info("FullSyncPostToEs end, total {}", total);
        } catch (Exception e) {
            log.error("FullSyncPostToEs failed, skip startup sync", e);
        }
    }
}
