package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.CommonConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.mapper.PostFavourMapper;
import com.ttbt.smartclass.mapper.PostMapper;
import com.ttbt.smartclass.mapper.PostThumbMapper;
import com.ttbt.smartclass.model.dto.post.PostEsDTO;
import com.ttbt.smartclass.model.dto.post.PostQueryRequest;
import com.ttbt.smartclass.model.entity.PostFavour;
import com.ttbt.smartclass.model.entity.PostThumb;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.PostVO;
import com.ttbt.smartclass.model.vo.UserVO;
import com.ttbt.smartclass.service.PostService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.SqlUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.ttbt.smartclass.model.entity.Post;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import com.ttbt.smartclass.esdao.PostEsDao;

/**
 * 帖子服务实现
*/
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final String ES_FIELD_IS_DELETE = "isDelete";

    private static final String ES_FIELD_USER_ID = "userId";

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private PostEsDao postEsDao;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        String type = post.getType();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags, type), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                SqlUtils.normalizeSortField(sortField));
        return queryWrapper;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery(ES_FIELD_IS_DELETE, 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(ES_FIELD_USER_ID, userId));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollUtil.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            addPostSearchQuery(boolQueryBuilder, searchText);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(SqlUtils.normalizeSortField(sortField));
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    private void addPostSearchQuery(BoolQueryBuilder boolQueryBuilder, String searchText) {
        String keyword = searchText.trim();
        String wildcardKeyword = "*" + keyword + "*";
        BoolQueryBuilder relevanceQuery = QueryBuilders.boolQuery();
        relevanceQuery.should(QueryBuilders.matchPhraseQuery("title", keyword).boost(8.0f));
        relevanceQuery.should(QueryBuilders.matchPhraseQuery("content", keyword).boost(5.0f));
        relevanceQuery.should(QueryBuilders.matchPhraseQuery("tags", keyword).boost(4.0f));
        relevanceQuery.should(QueryBuilders.wildcardQuery("title.keyword", wildcardKeyword).boost(10.0f));
        relevanceQuery.should(QueryBuilders.wildcardQuery("content.keyword", wildcardKeyword).boost(6.0f));
        relevanceQuery.should(QueryBuilders.wildcardQuery("tags.keyword", wildcardKeyword).boost(4.0f));

        if (containsChinese(keyword) && keyword.length() > 2) {
            String coreKeyword = resolveCoreChineseKeyword(keyword);
            BoolQueryBuilder coreQuery = QueryBuilders.boolQuery();
            coreQuery.should(QueryBuilders.matchPhraseQuery("title", coreKeyword).boost(3.0f));
            coreQuery.should(QueryBuilders.matchPhraseQuery("content", coreKeyword).boost(2.0f));
            coreQuery.should(QueryBuilders.matchPhraseQuery("tags", coreKeyword));
            coreQuery.minimumShouldMatch(1);
            relevanceQuery.should(QueryBuilders.matchPhraseQuery("title", coreKeyword).boost(3.0f));
            relevanceQuery.should(QueryBuilders.matchPhraseQuery("content", coreKeyword).boost(2.0f));
            relevanceQuery.should(QueryBuilders.matchPhraseQuery("tags", coreKeyword));
            relevanceQuery.must(coreQuery);
        } else {
            relevanceQuery.should(QueryBuilders.multiMatchQuery(keyword, "title", "content", "tags")
                    .operator(Operator.AND)
                    .boost(2.0f));
        }
        relevanceQuery.minimumShouldMatch(1);
        boolQueryBuilder.must(relevanceQuery);
    }

    private boolean containsChinese(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fa5') {
                return true;
            }
        }
        return false;
    }

    private String resolveCoreChineseKeyword(String keyword) {
        if (keyword.endsWith("老师") && keyword.length() > 2) {
            return keyword.substring(0, keyword.length() - 2);
        }
        return keyword;
    }

    @Override
    public Page<Post> searchFromEs(String searchText) {
        if (StringUtils.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索词不能为空");
        }
        
        // 默认分页参数
        long current = 1;
        long pageSize = 10;
        
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤已删除的帖子
        boolQueryBuilder.filter(QueryBuilders.termQuery(ES_FIELD_IS_DELETE, 0));
        
        // 按关键词检索
        addPostSearchQuery(boolQueryBuilder, searchText);
        
        // 排序默认按相关度
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        
        // 分页
        PageRequest pageRequest = PageRequest.of((int) (current - 1), (int) pageSize);
        
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSorts(sortBuilder)
                .build();

        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        
        // 查出结果后，从 db 获取最新动态数据
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream()
                    .map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
                    
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream()
                        .collect(Collectors.groupingBy(Post::getId));
                        
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        
        // 查询评论数量
        int commentCount = baseMapper.getCommentCount(postId);
        postVO.setCommentNum(commentCount);
        
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("post_id", postId);
            postThumbQueryWrapper.eq("user_id", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("post_id", postId);
            postFavourQueryWrapper.eq("user_id", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        
        // 2. 批量查询评论数量
        List<Long> postIdList = postList.stream().map(Post::getId).collect(Collectors.toList());
        Map<Long, Integer> postIdCommentCountMap = new HashMap<>();
        List<Map<String, Object>> commentCountList = baseMapper.batchGetCommentCount(postIdList);
        for (Map<String, Object> map : commentCountList) {
            Long postId = (Long) map.get("post_id");
            Integer count = ((Number) map.get("count")).intValue();
            postIdCommentCountMap.put(postId, count);
        }
        
        // 3. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("post_id", postIdList);
            postThumbQueryWrapper.eq("user_id", loginUser.getId());
            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("post_id", postIdList);
            postFavourQueryWrapper.eq("user_id", loginUser.getId());
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
        }
        
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            // 设置评论数
            postVO.setCommentNum(postIdCommentCountMap.getOrDefault(post.getId(), 0));
            return postVO;
        }).collect(Collectors.toList());
        
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    /**
     * 保存帖子到数据库，同时同步到ES
     * @param post 帖子
     * @return 结果
     */
    @Override
    public boolean savePost(Post post) {
        boolean result = this.save(post);
        if (result) {
            try {
                // 同步到ES
                PostEsDTO postEsDTO = PostEsDTO.objToDto(post);
                postEsDao.save(postEsDTO);
                log.info("同步新增帖子到ES成功, id={}", post.getId());
            } catch (Exception e) {
                log.error("同步新增帖子到ES失败", e);
            }
        }
        return result;
    }

    /**
     * 更新帖子，同时更新ES
     * @param post 帖子
     * @return 结果
     */
    @Override
    public boolean updatePost(Post post) {
        boolean result = this.updateById(post);
        if (result) {
            try {
                // 同步到ES
                PostEsDTO postEsDTO = PostEsDTO.objToDto(post);
                postEsDao.save(postEsDTO);
                log.info("同步更新帖子到ES成功, id={}", post.getId());
            } catch (Exception e) {
                log.error("同步更新帖子到ES失败", e);
            }
        }
        return result;
    }

    /**
     * 删除帖子，同时从ES删除
     * @param id 帖子id
     * @return 结果
     */
    @Override
    public boolean deletePost(Long id) {
        boolean result = this.removeById(id);
        if (result) {
            try {
                // 从ES中删除
                postEsDao.deleteById(id);
                log.info("同步删除帖子到ES成功, id={}", id);
            } catch (Exception e) {
                log.error("同步删除帖子到ES失败", e);
            }
        }
        return result;
    }

}




