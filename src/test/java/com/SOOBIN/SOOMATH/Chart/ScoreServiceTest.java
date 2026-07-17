package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.Member;
import com.SOOBIN.SOOMATH.Member.MemberRepository;
import com.SOOBIN.SOOMATH.Member.Separated;
import com.SOOBIN.SOOMATH.Member.SeparatedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private SeparatedRepository separatedRepository;

    @InjectMocks
    private ScoreService scoreService;

    private Member testMember;
    private Separated testSeparated;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setUsername("testuser");
        testMember.setDisplayName("홍길동");

        testSeparated = new Separated();
        testSeparated.setSCode("1-1");
        testSeparated.setSName("고1 수학 기본반");
        testSeparated.setSMonth(new ArrayList<>(List.of("10")));
    }

    @Test
    @DisplayName("성적 파일 파싱 및 저장 테스트")
    void parseAndSaveScoresTest() throws IOException {
        // given
        // TSV 형식의 성적 데이터 파일 시뮬레이션
        // 라인 1: 반코드-월(주차/회차 목록)
        // 라인 2: 유저네임(각 주차별 점수)
        String fileContent = "1-1(11)\t1.1\t1.2\t2.1\n" +
                "testuser\t85\t90\t95\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "score.tsv",
                "text/tab-separated-values",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        when(separatedRepository.findBysCode("1-1")).thenReturn(Optional.of(testSeparated));
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));

        // when
        scoreService.parseAndSaveScores(file);

        // then
        // 분반에 새로운 월("11")이 추가되었는지 확인
        assertThat(testSeparated.getSMonth()).contains("11");
        verify(separatedRepository, times(1)).save(testSeparated);

        // 3개의 성적이 정상적으로 저장되었는지 확인
        verify(scoreRepository, times(3)).save(any(Score.class));
    }

    @Test
    @DisplayName("학생 성적 통계 조회 테스트")
    void getStudentScoresTest() {
        // given
        String username = "testuser";
        
        Score score1 = new Score();
        score1.setMember(testMember);
        score1.setExam("1.1");
        score1.setScore(80);
        score1.setSeparated("1-1");
        score1.setMonth("11");

        Score score2 = new Score();
        score2.setMember(testMember);
        score2.setExam("1.2");
        score2.setScore(90);
        score2.setSeparated("1-1");
        score2.setMonth("11");

        // 다른 학생의 점수 (통계 산출에 포함됨)
        Member otherMember = new Member();
        otherMember.setUsername("otheruser");
        otherMember.setDisplayName("임꺽정");

        Score otherScore1 = new Score();
        otherScore1.setMember(otherMember);
        otherScore1.setExam("1.1");
        otherScore1.setScore(60);
        otherScore1.setSeparated("1-1");
        otherScore1.setMonth("11");

        Score otherScore2 = new Score();
        otherScore2.setMember(otherMember);
        otherScore2.setExam("1.2");
        otherScore2.setScore(100);
        otherScore2.setSeparated("1-1");
        otherScore2.setMonth("11");

        List<Score> studentScores = List.of(score1, score2);
        List<Score> allScores = List.of(score1, score2, otherScore1, otherScore2);

        when(scoreRepository.findByMemberUsername(username)).thenReturn(studentScores);
        when(scoreRepository.findByMonthAndSeparated("11", "1-1")).thenReturn(allScores);

        // when
        Map<String, Object> result = scoreService.getStudentScores(username);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get("labels")).isEqualTo(List.of("1.1", "1.2"));
        assertThat(result.get("minScores")).isEqualTo(List.of(60, 90)); // 1.1회차 최저: 60, 1.2회차 최저: 90
        assertThat(result.get("maxScores")).isEqualTo(List.of(80, 100)); // 1.1회차 최고: 80, 1.2회차 최고: 100
        assertThat(result.get("avgScores")).isEqualTo(List.of(70, 95)); // 1.1회차 평균: 70, 1.2회차 평균: 95
        assertThat(result.get("studentScores")).isEqualTo(List.of(80, 90)); // testuser의 점수 목록
    }
}
