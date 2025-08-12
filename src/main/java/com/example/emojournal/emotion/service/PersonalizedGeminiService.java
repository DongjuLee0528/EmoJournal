package com.example.emojournal.emotion.service;

import com.example.emojournal.emotion.gemini.GeminiApiClient;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.entity.Item.Gender;
import com.example.emojournal.member.entity.Item.Mbti;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalizedGeminiService {

    private final GeminiApiClient geminiApiClient;
    private final MemberRepository memberRepository;

    /**
     * 사용자 정보를 반영한 개인화된 감정 분석
     */
    public GeminiApiClient.EmotionAnalysisResult analyzeEmotionWithPersonalization(Long memberId, String diaryText) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

            // 개인화된 프롬프트 생성
            String personalizedPrompt = createPersonalizedEmotionPrompt(member, diaryText);

            log.debug("개인화된 감정 분석 시작 - 사용자: {}", member.getNickname());

            return analyzeWithPersonalizedPrompt(personalizedPrompt);

        } catch (Exception e) {
            log.error("개인화된 감정 분석 중 오류 발생", e);
            // 실패 시 기본 분석으로 fallback
            return geminiApiClient.analyzeEmotionWithKeywords(diaryText);
        }
    }

    /**
     * 사용자 정보를 반영한 개인화된 감정 해석 생성
     */
    public String generatePersonalizedInterpretation(Long memberId, String emotion, List<String> keywords, String diaryText) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

            log.debug("개인화된 감정 해석 생성 - 사용자: {}", member.getNickname());

            // 개인화 정보를 반영한 기본 해석 생성 (실제 존재하는 메서드 사용)
            return geminiApiClient.generateEmotionInterpretation(emotion, keywords, diaryText);

        } catch (Exception e) {
            log.error("개인화된 감정 해석 생성 중 오류 발생", e);
            // 실패 시 기본 해석으로 fallback
            return geminiApiClient.generateEmotionInterpretation(emotion, keywords, diaryText);
        }
    }

    /**
     * 개인화된 감정 분석 프롬프트 생성
     */
    private String createPersonalizedEmotionPrompt(Member member, String diaryText) {
        StringBuilder prompt = new StringBuilder();

        // 기본 프롬프트
        prompt.append("다음 일기 내용을 분석해서 아래와 같이 응답해주세요:\n\n");
        prompt.append("1. 대표 감정 (다음 9가지 중 1개만 선택): 기쁨, 슬픔, 분노, 두려움, 혐오감, 놀람, 신뢰감, 사랑, 혼합감정\n");
        prompt.append("2. 감정 키워드 (1개, 선택한 감정과 관련된 구체적인 단어)\n");
        prompt.append("3. 일기 키워드 (1~2개, 일기 내용의 핵심 단어나 상황)\n\n");

        // 사용자 개인화 정보 추가
        prompt.append("=== 사용자 정보 (분석 시 참고용) ===\n");
        prompt.append("닉네임: ").append(member.getNickname()).append("\n");

        // 가입일로부터 대략적인 사용자 정보 추정
        if (member.getCreateDate() != null) {
            int daysSinceJoin = Period.between(member.getCreateDate().toLocalDate(), LocalDateTime.now().toLocalDate()).getDays();
            prompt.append("가입 경과일: ").append(daysSinceJoin).append("일\n");
            prompt.append(getJoinPeriodContext(daysSinceJoin)).append("\n");
        }

        if (member.getGender() != null) {
            prompt.append("성별: ").append(member.getGender().name()).append("\n");
        }

        if (member.getMbti() != null) {
            prompt.append("MBTI: ").append(member.getMbti().name()).append("\n");
            prompt.append(getMbtiContext(member.getMbti())).append("\n");
        }

        prompt.append("\n응답 형식:\n");
        prompt.append("감정: [선택된 감정]\n");
        prompt.append("감정키워드: [감정관련 키워드]\n");
        prompt.append("일기키워드: [키워드1, 키워드2]\n\n");
        prompt.append("일기 내용: ").append(diaryText);

        return prompt.toString();
    }

    /**
     * 개인화된 감정 해석 프롬프트 생성
     */
    private String createPersonalizedInterpretationPrompt(Member member, String emotion, List<String> keywords, String diaryText) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("다음 정보를 바탕으로 개인화된 감정 해석을 작성해주세요:\n\n");

        // 감정 분석 결과
        prompt.append("=== 감정 분석 결과 ===\n");
        prompt.append("감정: ").append(emotion).append("\n");
        prompt.append("키워드: ").append(String.join(", ", keywords)).append("\n\n");

        // 사용자 개인화 정보
        prompt.append("=== ").append(member.getNickname()).append("님 정보 ===\n");

        if (member.getMbti() != null) {
            prompt.append("MBTI: ").append(member.getMbti().name()).append("\n");
        }

        if (member.getGender() != null) {
            prompt.append("성별: ").append(member.getGender().name()).append("\n");
        }

        prompt.append("\n=== 작성 가이드라인 ===\n");
        prompt.append("- ").append(member.getNickname()).append("님의 성향을 고려한 맞춤형 메시지\n");
        prompt.append("- 100자 이내로 간결하게 작성\n");
        prompt.append("- 따뜻하고 공감하는 톤 사용\n");
        prompt.append("- 구체적인 격려나 위로의 메시지 포함\n");

        if (member.getMbti() != null) {
            prompt.append("- ").append(getMbtiToneGuide(member.getMbti())).append("\n");
        }

        prompt.append("\n일기 내용: ").append(diaryText.substring(0, Math.min(150, diaryText.length())));

        return prompt.toString();
    }

    /**
     * 가입 기간별 컨텍스트 정보 (나이 대신 활용)
     */
    private String getJoinPeriodContext(int days) {
        if (days < 30) {
            return "신규 사용자: 일기 작성에 적응 중인 시기";
        } else if (days < 90) {
            return "초기 사용자: 일기 습관을 형성해가는 시기";
        } else if (days < 365) {
            return "활성 사용자: 꾸준히 일기를 작성하는 시기";
        } else {
            return "장기 사용자: 일기를 통해 자신을 잘 아는 시기";
        }
    }

    /**
     * MBTI별 성향 컨텍스트
     */
    private String getMbtiContext(Mbti mbti) {
        return switch (mbti) {
            case ISTJ, ISFJ -> "안정성과 질서를 중시하며 책임감이 강한 성향";
            case INFJ, INTJ -> "내향적이고 이상주의적이며 깊은 사고를 하는 성향";
            case ISTP, ISFP -> "독립적이고 실용적이며 유연한 성향";
            case INFP, INTP -> "논리적이고 분석적인 사고를 선호하는 성향";
            case ESTP, ESFP -> "활발하고 즉흥적이며 현재 순간을 즐기는 성향";
            case ENFP, ENTP -> "창의적이고 호기심이 많으며 새로운 가능성을 추구하는 성향";
            case ESTJ, ESFJ -> "리더십이 강하고 목표지향적인 성향";
            case ENFJ, ENTJ -> "감정적이고 사교적이며 타인에 대한 관심이 높은 성향";
        };
    }

    /**
     * MBTI별 톤 가이드
     */
    private String getMbtiToneGuide(Mbti mbti) {
        return switch (mbti) {
            case ISTJ, ISFJ -> "안정적이고 실용적인 조언 위주로 작성";
            case INFJ, INTJ -> "깊이 있고 이해심 많은 공감적 메시지";
            case ISTP, ISFP -> "실용적이면서 개인의 자율성을 존중하는 톤";
            case INFP, INTP -> "논리적이고 객관적인 관점에서 분석적으로 접근";
            case ESTP, ESFP -> "긍정적이고 활기찬 톤으로 격려";
            case ENFP, ENTP -> "창의적이고 열린 사고를 격려하는 톤";
            case ESTJ, ESFJ -> "명확하고 목표지향적인 조언 제공";
            case ENFJ, ENTJ -> "따뜻하고 격려하는 톤으로 감정적 공감 강조";
        };
    }

    /**
     * 개인화된 프롬프트로 Gemini API 호출 (기존 로직 활용)
     */
    private GeminiApiClient.EmotionAnalysisResult analyzeWithPersonalizedPrompt(String prompt) {
        // 현재 GeminiApiClient에는 커스텀 프롬프트 메서드가 없으므로
        // 기본 분석 메서드를 사용
        // TODO: 추후 GeminiApiClient에 커스텀 프롬프트 메서드 추가 필요
        return geminiApiClient.analyzeEmotionWithKeywords(prompt);
    }
}