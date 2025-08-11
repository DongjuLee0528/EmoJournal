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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 새로 추가할 생년월일 필드
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // 프로필 완성 여부 체크 (추가 정보 입력 완료 여부)
    @Column(name = "profile_completed", nullable = false)
    private Boolean profileCompleted = false;

    @CreatedDate
    private LocalDateTime createDate;

    @OneToMany(mappedBy = "member")
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Builder
    public Member(Long id, String email, String nickname, OAuthProvider oAuthProvider) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
        this.profileCompleted = false; // 초기에는 false
    }

    // 나이 계산 메서드
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // 프로필 완성 체크 메서드
    public boolean isProfileComplete() {
        return birthDate != null && gender != null && mbti != null;
    }

    // 프로필 완성 상태 업데이트
    public void updateProfileCompletedStatus() {
        this.profileCompleted = isProfileComplete();
    }

    public static MemberResponseDto fromEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .oAuthProvider(member.getOAuthProvider() != null ? member.getOAuthProvider().toString() : null)
                .createDate(member.createDate != null ? member.createDate.toString() : null)
                .birthDate(member.getBirthDate())
                .age(member.getAge())
                .gender(member.getGender() != null ? member.getGender().name() : null)  // enum을 String으로 변환
                .mbti(member.getMbti() != null ? member.getMbti().name() : null)        // enum을 String으로 변환
                .profileCompleted(member.getProfileCompleted())
                .build();
    }
}