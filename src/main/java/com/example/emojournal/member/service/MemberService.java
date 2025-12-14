package com.example.emojournal.member.service;

import com.example.emojournal.member.dto.requst.MemberUpdateRequest;
import com.example.emojournal.member.entity.Item.Gender;
import com.example.emojournal.member.entity.Item.Mbti;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.entity.exception.MemberNotFoundException;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/**
 * 회원 정보 관리를 담당하는 서비스 클래스
 *
 * 회원의 기본 정보 조회와 프로필 업데이트 기능을 제공합니다.
 * MBTI, 성별, 닉네임 등의 개인화 설정을 관리합니다.
 *
 * 주요 기능:
 * - 회원 ID로 회원 정보 조회
 * - 회원 프로필 업데이트 (MBTI, 성별, 닉네임)
 * - 회월 존재성 검증 및 예외 처리
 * - 트랜잭션 기반 데이터 일관성 보장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    /** 회원 정보 데이터베이스 접근을 담당하는 JPA 리포지토리 */
    public final MemberRepository memberRepository;

    /**
     * 회원 ID로 회원 정보를 조회합니다.
     *
     * 주어진 ID에 해당하는 회원 정보를 데이터베이스에서 조회합니다.
     * 읽기 전용 트랜잭션으로 설정되어 성능 최적화를 지원합니다.
     *
     * @param id 조회할 회원의 고유 식별자
     * @return 조회된 회원 엔티티
     * @throws MemberNotFoundException 해당 ID의 회원을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다. id= " + id));
    }

    /**
     * 회원의 프로필 정보를 업데이트합니다.
     *
     * 회원의 MBTI, 성별, 닉네임 등의 개인 정보를 업데이트합니다.
     * 문자열을 대문자로 변환하여 열거형 값과 일치시키고,
     * 트랜잭션 내에서 실행되어 데이터 일관성을 보장합니다.
     *
     * @param memberUpdateRequest 업데이트할 회원 정보 요청 객체
     * @param memberId 업데이트할 회원의 고유 식별자
     * @return 업데이트된 회원 엔티티
     * @throws MemberNotFoundException 해당 ID의 회원을 찾을 수 없는 경우
     * @throws IllegalArgumentException MBTI나 성별 값이 잘못된 경우
     */
    @Transactional
    public Member setMember(MemberUpdateRequest memberUpdateRequest,Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다. id= " + memberId));

        // MBTI 및 성별 열거형 값으로 변환 (대소문자 구분 없이)
        member.setMbti(Mbti.valueOf(memberUpdateRequest.getMbti().toUpperCase()));
        member.setGender(Gender.valueOf(memberUpdateRequest.getGender().toUpperCase()));
        member.setNickname(memberUpdateRequest.getNickname());

        log.info("회원 정보 업데이트 - memberId: {}, mbti: {}, gender: {}", memberId, member.getMbti(), member.getGender());

        return member;
    }

}
