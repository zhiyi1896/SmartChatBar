package com.niu.community.profile.vo;

import java.util.Set;
import lombok.Data;

@Data
public class UserProfileVO {
    private Long userId;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private String role;
    private Integer receivedLikeCount;
    private Long postCount;
    private Long followerCount;
    private Long followingCount;
    private Boolean followed;
    private Set<String> badges;
}
