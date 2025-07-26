package com.example.emojournal.member.service;

import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.entity.exception.MemberNotFoundException;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    public final MemberRepository memberRepository;

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다. id= " + id));
    }


}
