package com.niu.community.profile.service;

import com.niu.community.auth.service.AuthService;
import com.niu.community.cache.service.RedisJsonCacheService;
import com.niu.community.common.exception.BusinessException;
import com.niu.community.follow.service.FollowService;
import com.niu.community.post.entity.PostEntity;
import com.niu.community.post.mapper.PostMapper;
import com.niu.community.post.vo.PostVO;
import com.niu.community.profile.dto.UpdateEmailRequest;
import com.niu.community.profile.dto.UpdatePasswordRequest;
import com.niu.community.profile.dto.UpdateProfileRequest;
import com.niu.community.profile.vo.UserProfileVO;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private static final Duration PROFILE_TTL = Duration.ofMinutes(10);
    private static final Duration EMPTY_TTL = Duration.ofSeconds(60);
    private static final Duration REBUILD_LOCK_TTL = Duration.ofSeconds(30);

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final FollowService followService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender javaMailSender;
    private final AuthService authService;
    private final RedisJsonCacheService redisJsonCacheService;
    private final Executor cacheRebuildExecutor;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public ProfileService(UserMapper userMapper, PostMapper postMapper, FollowService followService,
                          PasswordEncoder passwordEncoder, StringRedisTemplate redisTemplate, JavaMailSender javaMailSender,
                          AuthService authService, RedisJsonCacheService redisJsonCacheService,
                          @Qualifier("cacheRebuildExecutor") Executor cacheRebuildExecutor) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.followService = followService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.javaMailSender = javaMailSender;
        this.authService = authService;
        this.redisJsonCacheService = redisJsonCacheService;
        this.cacheRebuildExecutor = cacheRebuildExecutor;
    }

    public UserProfileVO getProfile(Long profileUserId, Long currentUserId) {
        String cacheKey = profileCacheKey(profileUserId);
        String cached = redisJsonCacheService.getRaw(cacheKey);
        UserProfileVO vo;
        if (RedisJsonCacheService.NULL_MARKER.equals(cached)) {
            throw new BusinessException("USER_NOT_FOUND");
        }
        if (cached != null) {
            RedisJsonCacheService.CachePayload<UserProfileVO> payload = redisJsonCacheService.readPayload(cached, UserProfileVO.class);
            vo = payload.data();
            if (payload.expired()) {
                rebuildProfileAsync(profileUserId);
            }
        } else {
            UserEntity user = userMapper.selectById(profileUserId);
            if (user == null) {
                redisJsonCacheService.writeNull(cacheKey, EMPTY_TTL);
                throw new BusinessException("USER_NOT_FOUND");
            }
            vo = buildProfileSummary(user, profileUserId);
            redisJsonCacheService.writeWithLogicalExpire(cacheKey, vo, PROFILE_TTL);
        }
        UserProfileVO result = copyProfile(vo);
        result.setFollowed(currentUserId != null && followService.getFollowingIds(currentUserId).contains(profileUserId));
        return result;
    }

    public void evictProfileCache(Long userId) {
        redisJsonCacheService.delete(profileCacheKey(userId));
    }

    public List<PostVO> getUserPosts(Long userId) {
        UserEntity user = requireUser(userId);
        return postMapper.selectList(com.baomidou.mybatisplus.core.toolkit.Wrappers.<PostEntity>lambdaQuery()
                .eq(PostEntity::getUserId, userId)
                .eq(PostEntity::getStatus, 1)
                .orderByDesc(PostEntity::getCreateTime))
            .stream().map(post -> {
                PostVO vo = new PostVO();
                vo.setId(post.getId());
                vo.setUserId(post.getUserId());
                vo.setTitle(post.getTitle());
                vo.setContent(post.getContent());
                vo.setAuthorName(user.getNickname());
                vo.setAuthorAvatar(user.getAvatar());
                vo.setCreateTime(post.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                vo.setViewCount(post.getViewCount());
                vo.setLikeCount(post.getLikeCount());
                vo.setCommentCount(post.getCommentCount());
                vo.setLiked(false);
                vo.setAuthor(true);
                return vo;
            }).toList();
    }

    @Transactional
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = requireUser(userId);
        user.setNickname(request.getNickname());
        user.setBio(request.getBio());
        user.setAvatar(request.getAvatar());
        userMapper.updateById(user);
        evictProfileCache(userId);
        return getProfile(userId, userId);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserEntity user = requireUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("PASSWORD_INVALID");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        authService.invalidateUserSessions(userId);
    }

    public void sendEmailCode(Long userId, String email) {
        requireUser(userId);
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        redisTemplate.opsForValue().set("UPDATE_EMAIL_CODE:" + userId + ":" + email, code, 5, TimeUnit.MINUTES);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Email verification");
        message.setText("Code: " + code + ", valid for 5 minutes.");
        javaMailSender.send(message);
    }

    @Transactional
    public void updateEmail(Long userId, UpdateEmailRequest request) {
        UserEntity user = requireUser(userId);
        String cacheCode = redisTemplate.opsForValue().get("UPDATE_EMAIL_CODE:" + userId + ":" + request.getEmail());
        if (cacheCode == null || !cacheCode.equals(request.getCode())) {
            throw new BusinessException("CODE_INVALID");
        }
        user.setEmail(request.getEmail());
        userMapper.updateById(user);
        redisTemplate.delete("UPDATE_EMAIL_CODE:" + userId + ":" + request.getEmail());
        evictProfileCache(userId);
    }

    private void rebuildProfileAsync(Long userId) {
        String lockKey = profileCacheKey(userId) + ":lock";
        if (!redisJsonCacheService.tryLock(lockKey, REBUILD_LOCK_TTL)) {
            return;
        }
        cacheRebuildExecutor.execute(() -> {
            try {
                UserEntity user = userMapper.selectById(userId);
                if (user == null) {
                    redisJsonCacheService.writeNull(profileCacheKey(userId), EMPTY_TTL);
                    return;
                }
                UserProfileVO summary = buildProfileSummary(user, userId);
                redisJsonCacheService.writeWithLogicalExpire(profileCacheKey(userId), summary, PROFILE_TTL);
            } finally {
                redisJsonCacheService.unlock(lockKey);
            }
        });
    }

    private String profileCacheKey(Long userId) {
        return "profile:summary:" + userId;
    }

    private UserProfileVO buildProfileSummary(UserEntity user, Long profileUserId) {
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        vo.setPostCount(postMapper.selectCount(com.baomidou.mybatisplus.core.toolkit.Wrappers.<PostEntity>lambdaQuery()
            .eq(PostEntity::getUserId, profileUserId)
            .eq(PostEntity::getStatus, 1)));
        Long followerCount = redisTemplate.opsForSet().size("follow:follower:" + profileUserId);
        Long followingCount = redisTemplate.opsForSet().size("follow:following:" + profileUserId);
        vo.setFollowerCount(followerCount == null ? 0L : followerCount);
        vo.setFollowingCount(followingCount == null ? 0L : followingCount);
        vo.setFollowed(false);
        Set<String> badges = new HashSet<>();
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            badges.add("ADMIN");
        }
        if ((user.getReceivedLikeCount() == null ? 0 : user.getReceivedLikeCount()) >= 100) {
            badges.add("POPULAR_AUTHOR");
        }
        vo.setBadges(badges);
        return vo;
    }

    private UserProfileVO copyProfile(UserProfileVO source) {
        UserProfileVO target = new UserProfileVO();
        BeanUtils.copyProperties(source, target);
        if (source.getBadges() != null) {
            target.setBadges(new HashSet<>(source.getBadges()));
        }
        return target;
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND");
        }
        return user;
    }
}