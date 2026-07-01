package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.UserLevelMapper;
import com.ttbt.smartclass.model.dto.ExpAddRequest;
import com.ttbt.smartclass.model.entity.LevelConfig;
import com.ttbt.smartclass.model.entity.UserExpRecord;
import com.ttbt.smartclass.model.entity.UserLevel;
import com.ttbt.smartclass.model.enums.ActionTypeEnum;
import com.ttbt.smartclass.model.vo.UserExpRecordVO;
import com.ttbt.smartclass.model.vo.UserLevelVO;
import com.ttbt.smartclass.service.AchievementService;
import com.ttbt.smartclass.service.LevelConfigService;
import com.ttbt.smartclass.service.UserExpRecordService;
import com.ttbt.smartclass.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户等级服务实现类
 */
@Slf4j
@Service
public class UserLevelServiceImpl extends ServiceImpl<UserLevelMapper, UserLevel> implements UserLevelService {

    @Resource
    private UserLevelMapper userLevelMapper;

    @Resource
    private LevelConfigService levelConfigService;

    @Resource
    private UserExpRecordService userExpRecordService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AchievementService achievementService;

    private static final String EXP_LIMIT_KEY_PREFIX = "user:exp:limit:";
    private static final String EXP_CACHE_KEY_PREFIX = "user:exp:";
    private static final String DAILY_LOGIN_KEY_PREFIX = "user:daily:login:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 为用户增加指定行为对应的经验值。
     *
     * @param request 经验增加请求
     * @return 实际增加的经验值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addExp(ExpAddRequest request) {
        // 解析用户 id 和行为类型，行为类型不存在时拒绝加经验
        Long userId = request.getUserId();
        String actionTypeCode = request.getActionType();

        ActionTypeEnum actionType = ActionTypeEnum.getByCode(actionTypeCode);
        if (actionType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid action type");
        }

        // 校验该行为当天获取经验次数是否达到上限
        if (!checkDailyLimit(userId, actionType)) {
            return 0;
        }

        // 对带 relatedId 的行为做去重，避免同一对象重复领取经验
        if (!checkDuplicateAction(userId, actionType, request.getRelatedId())) {
            return 0;
        }

        // 按行为配置计算本次经验值，非正数直接忽略
        int expToAdd = calculateExp(actionType, request);
        if (expToAdd <= 0) {
            return 0;
        }

        // 先确保用户等级记录存在，避免更新经验时影响 0 行。

        getUserLevelEntity(userId);
        // 更新 Redis 中的每日次数，保存经验流水，并累加用户总经验
        updateRedisCount(userId, actionType);
        saveExpRecord(userId, actionType, expToAdd, request);
        userLevelMapper.addExp(userId, expToAdd);
        // 根据最新经验判断是否升级，并清理用户等级缓存
        checkAndHandleLevelUp(userId);
        clearUserExpCache(userId);

        // 经验关联具体业务对象时，同步触发成就进度检查，失败不影响经验发放
        if (request.getRelatedId() != null) {
            try {
                achievementService.checkAndUpdateUserAchievement(userId, actionTypeCode, request.getRelatedId());
            } catch (Exception e) {
                log.warn("更新用户成就失败，继续保留经验记录，user_id={}, actionType={}", userId, actionTypeCode, e);
            }
        }

        return expToAdd;
    }

    /**
     * 查询用户等级信息。
     *
     * @param userId 用户 ID
     * @return 用户等级展示信息
     */
    @Override
    public UserLevelVO getUserLevel(Long userId) {
        // 优先读取 Redis 等级缓存，减少频繁查询数据库
        String cacheKey = EXP_CACHE_KEY_PREFIX + userId;
        try {
            UserLevelVO cached = (UserLevelVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("读取用户等级缓存失败，将查询数据库，user_id={}", userId, e);
        }

        // 缓存不存在时查询数据库，没有等级记录则创建默认等级
        LambdaQueryWrapper<UserLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLevel::getUserId, userId);
        UserLevel userLevel = userLevelMapper.selectOne(queryWrapper);
        if (userLevel == null) {
            userLevel = createDefaultUserLevel(userId);
        }

        // 转换为 VO 后写入短期缓存，便于个人中心频繁展示
        UserLevelVO userLevelVO = convertToVO(userLevel);
        try {
            redisTemplate.opsForValue().set(cacheKey, userLevelVO, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入用户等级缓存失败，user_id={}", userId, e);
        }
        return userLevelVO;
    }

    /**
     * 分页查询用户经验变动记录。
     *
     * @param userId 用户 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @return 经验记录列表
     */
    @Override
    public List<UserExpRecordVO> getExpRecords(Long userId, long current, long pageSize) {
        // 按创建时间倒序查询用户经验流水
        Page<UserExpRecord> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<UserExpRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserExpRecord::getUserId, userId)
                .orderByDesc(UserExpRecord::getCreateTime);

        // 将流水实体转换为前端展示 VO，并补充行为描述
        Page<UserExpRecord> resultPage = userExpRecordService.page(page, queryWrapper);
        return resultPage.getRecords().stream()
                .map(this::convertToRecordVO)
                .collect(Collectors.toList());
    }

    /**
     * 处理用户每日登录经验奖励。
     *
     * @param userId 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDailyLogin(Long userId) {
        // 使用用户和日期生成每日登录标记 key，避免同一天重复领取登录经验
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);

        String loginKey = DAILY_LOGIN_KEY_PREFIX + userId + ":" + todayStr;
        Boolean hasLogin = false;
        try {
            hasLogin = (Boolean) redisTemplate.opsForValue().get(loginKey);
        } catch (Exception e) {
            log.warn("读取每日登录标记失败，继续处理登录奖励，user_id={}", userId, e);
        }
        if (Boolean.TRUE.equals(hasLogin)) {
            return;
        }

        // 发放每日登录经验奖励
        ExpAddRequest loginRequest = new ExpAddRequest();
        loginRequest.setUserId(userId);
        loginRequest.setActionType(ActionTypeEnum.DAILY_LOGIN.getCode());
        loginRequest.setDescription("daily login reward");
        addExp(loginRequest);

        // 写入 24 小时 Redis 标记，防止当天重复领取登录奖励
        try {
            redisTemplate.opsForValue().set(loginKey, true, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入每日登录标记失败，user_id={}", userId, e);
        }

        // 更新连续登录天数，并按连续天数发放额外奖励
        checkContinuousLogin(userId);
    }

    /**
     * 检查并更新用户连续登录天数。
     *
     * @param userId 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkContinuousLogin(Long userId) {
        // 获取或创建用户等级记录，并读取上次登录日期
        UserLevel userLevel = getUserLevelEntity(userId);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastLoginDate = null;

        if (userLevel.getLastLoginDate() != null) {
            lastLoginDate = userLevel.getLastLoginDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }

        // 根据上次登录日期计算连续登录天数：昨天登录则累加，断签则重置为 1
        int continuousDays = userLevel.getContinuousLoginDays() == null ? 0 : userLevel.getContinuousLoginDays();
        if (lastLoginDate == null) {
            continuousDays = 1;
        } else if (lastLoginDate.equals(yesterday)) {
            continuousDays++;
        } else if (!lastLoginDate.equals(today)) {
            continuousDays = 1;
        }

        // 持久化连续登录天数和本次登录日期
        userLevel.setContinuousLoginDays(continuousDays);
        userLevel.setLastLoginDate(java.sql.Date.valueOf(today));
        userLevelMapper.updateById(userLevel);

        // 根据连续登录天数发放 1、3、7 天等额外奖励
        giveContinuousLoginReward(userId, continuousDays);
    }

    private boolean checkDailyLimit(Long userId, ActionTypeEnum actionType) {
        if (!actionType.hasDailyLimit()) {
            return true;
        }

        String limitKey = getDailyLimitKey(userId, actionType);
        try {
            Integer currentCount = (Integer) redisTemplate.opsForValue().get(limitKey);
            if (currentCount == null) {
                return true;
            }
            return currentCount < actionType.getDailyLimit();
        } catch (Exception e) {
            log.warn("检查每日经验次数限制失败，默认允许本次加经验，user_id={}, actionType={}", userId, actionType.getCode(), e);
            return true;
        }
    }

    private boolean checkDuplicateAction(Long userId, ActionTypeEnum actionType, Long relatedId) {
        if (relatedId == null) {
            return true;
        }

        String duplicateKey = EXP_LIMIT_KEY_PREFIX + userId + ":" + actionType.getCode() + ":" + relatedId;
        try {
            Boolean exists = (Boolean) redisTemplate.opsForValue().get(duplicateKey);
            if (Boolean.TRUE.equals(exists)) {
                return false;
            }
            redisTemplate.opsForValue().set(duplicateKey, true, 24, TimeUnit.HOURS);
            return true;
        } catch (Exception e) {
            log.warn("检查经验获取次数限制失败，默认允许本次加经验，user_id={}, actionType={}, relatedId={}",
                    userId, actionType.getCode(), relatedId, e);
            return true;
        }
    }

    private int calculateExp(ActionTypeEnum actionType, ExpAddRequest request) {
        return actionType.getExpPerAction();
    }

    private void updateRedisCount(Long userId, ActionTypeEnum actionType) {
        String limitKey = getDailyLimitKey(userId, actionType);
        try {
            redisTemplate.opsForValue().increment(limitKey);
            long ttl = getSecondsUntilMidnight();
            redisTemplate.expire(limitKey, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("更新每日经验次数缓存失败，user_id={}, actionType={}", userId, actionType.getCode(), e);
        }
    }

    private void saveExpRecord(Long userId, ActionTypeEnum actionType, int exp, ExpAddRequest request) {
        UserExpRecord record = new UserExpRecord();
        record.setUserId(userId);
        record.setActionType(actionType.getCode());
        record.setExpChange(exp);
        record.setDescription(Optional.ofNullable(request.getDescription()).orElse(actionType.getDescription()));
        record.setRelatedId(request.getRelatedId());
        record.setRelatedType(request.getRelatedType());
        record.setIpAddress(request.getIpAddress());
        record.setDeviceInfo(request.getDeviceInfo());
        userExpRecordService.save(record);
    }

    private void checkAndHandleLevelUp(Long userId) {
        UserLevel userLevel = getUserLevelEntity(userId);
        List<LevelConfig> levelConfigs = levelConfigService.getEnabledConfigs();
        if (levelConfigs == null || levelConfigs.isEmpty()) {
            return;
        }

        levelConfigs.sort(Comparator.comparing(LevelConfig::getLevel));
        boolean leveledUp = false;
        while (true) {
            Optional<LevelConfig> nextLevelOpt = levelConfigs.stream()
                    .filter(config -> config.getLevel() > userLevel.getLevel())
                    .filter(config -> userLevel.getExp() >= config.getRequiredExp())
                    .findFirst();

            if (!nextLevelOpt.isPresent()) {
                break;
            }

            LevelConfig nextLevel = nextLevelOpt.get();
            userLevel.setLevel(nextLevel.getLevel());
            userLevel.setNextLevelExp(getNextLevelExp(levelConfigs, nextLevel.getLevel()));
            userLevel.setLevelUpTime(new java.util.Date());
            userLevelMapper.updateById(userLevel);
            leveledUp = true;

            sendLevelUpNotification(userId, nextLevel);
        }

        if (leveledUp) {
            clearUserExpCache(userId);
        }
    }

    private Integer getNextLevelExp(List<LevelConfig> configs, Integer currentLevel) {
        return configs.stream()
                .filter(config -> config.getLevel() > currentLevel)
                .min(Comparator.comparing(LevelConfig::getLevel))
                .map(LevelConfig::getRequiredExp)
                .orElse(Integer.MAX_VALUE);
    }

    private void giveContinuousLoginReward(Long userId, int days) {
        ActionTypeEnum rewardType = null;
        if (days == 1) {
            rewardType = ActionTypeEnum.LOGIN_DAY_1;
        } else if (days == 3) {
            rewardType = ActionTypeEnum.LOGIN_DAY_3;
        } else if (days == 7) {
            rewardType = ActionTypeEnum.LOGIN_DAY_7;
        }

        if (rewardType == null) {
            return;
        }

        ExpAddRequest request = new ExpAddRequest();
        request.setUserId(userId);
        request.setActionType(rewardType.getCode());
        request.setDescription("continuous login day " + days + " reward");
        addExp(request);
    }

    private UserLevel createDefaultUserLevel(Long userId) {
        UserLevel userLevel = new UserLevel();
        userLevel.setUserId(userId);
        userLevel.setLevel(1);
        userLevel.setExp(0);
        userLevel.setNextLevelExp(100);
        userLevel.setTotalExp(0);
        userLevel.setContinuousLoginDays(0);
        userLevelMapper.insert(userLevel);
        return userLevel;
    }

    private UserLevelVO convertToVO(UserLevel userLevel) {
        UserLevelVO vo = new UserLevelVO();
        BeanUtils.copyProperties(userLevel, vo);

        LevelConfig config = levelConfigService.getByLevel(userLevel.getLevel());
        if (config != null) {
            vo.setLevelName(config.getLevelName());
            vo.setIconUrl(config.getIconUrl());
            vo.setPrivilegeDesc(config.getPrivilegeDesc());
        }

        if (userLevel.getNextLevelExp() != null && userLevel.getNextLevelExp() != Integer.MAX_VALUE) {
            double progress = (double) userLevel.getExp() / userLevel.getNextLevelExp() * 100;
            vo.setProgressPercent(Math.min(progress, 100.0));
            vo.setExpToNextLevel(Math.max(0, userLevel.getNextLevelExp() - userLevel.getExp()));
        } else {
            vo.setProgressPercent(100.0);
            vo.setExpToNextLevel(0);
        }

        return vo;
    }

    private UserExpRecordVO convertToRecordVO(UserExpRecord record) {
        UserExpRecordVO vo = new UserExpRecordVO();
        BeanUtils.copyProperties(record, vo);
        ActionTypeEnum actionType = ActionTypeEnum.getByCode(record.getActionType());
        if (actionType != null) {
            vo.setActionDescription(actionType.getDescription());
        }
        return vo;
    }

    private UserLevel getUserLevelEntity(Long userId) {
        LambdaQueryWrapper<UserLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLevel::getUserId, userId);
        UserLevel userLevel = userLevelMapper.selectOne(queryWrapper);
        if (userLevel == null) {
            userLevel = createDefaultUserLevel(userId);
        }
        return userLevel;
    }

    private void clearUserExpCache(Long userId) {
        String cacheKey = EXP_CACHE_KEY_PREFIX + userId;
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("清除用户等级缓存失败，user_id={}", userId, e);
        }
    }

    private String getDailyLimitKey(Long userId, ActionTypeEnum actionType) {
        return EXP_LIMIT_KEY_PREFIX + userId + ":" + actionType.getCode();
    }

    private long getSecondsUntilMidnight() {
        LocalDateTime midnight = LocalDateTime.now().plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return LocalDateTime.now().until(midnight, java.time.temporal.ChronoUnit.SECONDS);
    }

    private void sendLevelUpNotification(Long userId, LevelConfig newLevel) {
        log.info("用户等级提升通知：用户 {} 升级为 {}", userId, newLevel.getLevelName());
    }
}
