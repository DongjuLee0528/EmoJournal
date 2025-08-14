package com.example.emojournal.member.service;

import com.example.emojournal.member.dto.BirthDateDto;
import com.example.emojournal.member.dto.requst.MemberUpdateRequest;
import com.example.emojournal.member.entity.Item.Gender;
import com.example.emojournal.member.entity.Item.Mbti;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.entity.embedded.BirthDate;
import com.example.emojournal.member.entity.exception.MemberNotFoundException;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    public final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다. id= " + id));
    }

    @Transactional
    public Member setMember(MemberUpdateRequest memberUpdateRequest,Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다. id= " + memberId));

        member.setMbti(Mbti.valueOf(memberUpdateRequest.getMbti().toUpperCase()));
        member.setGender(Gender.valueOf(memberUpdateRequest.getGender().toUpperCase()));
        member.setNickname(memberUpdateRequest.getNickname());

        BirthDateDto birthDate = memberUpdateRequest.getBirthDate();
//        if (birthDate == null || birthDate.getYear() == null || birthDate.getMonth() == null || birthDate.getDay() == null) {
//            throw new InvalidBirthDateException("생년월일 정보가 올바르지 않습니다.");
//        }

        member.setBirthDate(new BirthDate(birthDate.getYear(), birthDate.getMonth(), birthDate.getDay()));

        log.info("mbti : " + member.getMbti().toString());
        log.info("gender : " + member.getGender().toString());

        return member;
    }

}
