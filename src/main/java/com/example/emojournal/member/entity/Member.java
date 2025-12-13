package com.example.emojournal.member.entity;

import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import com.example.emojournal.member.dto.MemberResponseDto;
import com.example.emojournal.member.entity.Item.Gender;
import com.example.emojournal.member.entity.Item.Mbti;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 정보를 관리하는 JPA 엔티티 클래스
 *
 * OAuth 기반 로그인을 통해 가입한 사용자의 기본 정보와
 * 개인화 설정(MBTI, 성별)을 저장하고 관리합니다.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Member {

    /** 회원 고유 식별자 (기본키) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    /** 회원 이메일 주소 */
    private String email;

    /** 회원 닉네임 */
    private String nickname;

    /** OAuth 제공자 (GOOGLE 등) */
    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    /** 회원 MBTI 유형 */
    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    /** 회원 성별 */
    @Enumerated(EnumType.STRING)
    private Gender gender;

    /** 회원 가입일시 */
    @CreatedDate
    private LocalDateTime createDate;

    /** 회원과 연관된 리프레시 토큰 목록 */
    @OneToMany(mappedBy = "member")
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    /** 프로필 완성도 (0: 미완성, 1: 완성) */
    private Integer profile_completed;

    /**
     * Member 엔티티 생성자
     *
     * @param id 회원 ID
     * @param email 이메일 주소
     * @param nickname 닉네임
     * @param oAuthProvider OAuth 제공자
     */
    @Builder
    public Member(Long id, String email, String nickname, OAuthProvider oAuthProvider) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
    }

    /**
     * Member 엔티티를 MemberResponseDto로 변환하는 정적 팩토리 메서드
     *
     * @param member 변환할 Member 엔티티
     * @return 변환된 MemberResponseDto 객체
     */
    public static MemberResponseDto fromEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .oAuthProvider(member.getOAuthProvider().toString())
                .createDate(member.createDate.toString())
                .gender(String.valueOf(member.getGender()))
                .mbti(String.valueOf(member.getMbti()))
                .build();
    }
    
}
