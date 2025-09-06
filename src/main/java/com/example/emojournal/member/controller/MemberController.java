package com.example.emojournal.member.controller;

import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import com.example.emojournal.member.dto.requst.MemberUpdateRequest;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.dto.MemberResponseDto;
import com.example.emojournal.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member")
    public MemberResponseDto showMember(HttpServletRequest request) {

        Long memberId = AuthenticationContextHolder.getContext();

        log.info("memberId : " + memberId);

        MemberResponseDto memberResponseDto = Member.fromEntity(memberService.findMemberById(memberId));

        log.info("memberResponseDto : " + memberResponseDto);

        return memberResponseDto;
    }

    @PutMapping("/member")
    public ResponseEntity<MemberResponseDto> setMember(@RequestBody MemberUpdateRequest memberUpdateRequest,HttpServletRequest request) {

        Long memberId = AuthenticationContextHolder.getContext();

        log.info("memberUpdateRequest : "+ memberUpdateRequest.toString());

        Member member = memberService.setMember(memberUpdateRequest, memberId);

        MemberResponseDto memberResponseDto = Member.fromEntity(member);

        log.info("memberResponseDto : " + memberResponseDto.toString());

        return ResponseEntity.ok()
                .body(memberResponseDto);
    }
}
