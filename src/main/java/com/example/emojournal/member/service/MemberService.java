package com.example.emojournal.member.service;

import com.example.emojournal.member.dto.requst.MemberUpdateRequest;
import com.example.emojournal.member.entity.Item.Gender;
import com.example.emojournal.member.entity.Item.Mbti;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.entity.exception.MemberNotFoundException;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

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

        return member;
    }

}
