package com.SOOBIN.SOOMATH.QnaBoard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QnaPostServiceTest {

    @Mock
    private QnaPostRepository qnaPostRepository;

    @InjectMocks
    private QnaPostService qnaPostService;

    @Test
    @DisplayName("Q&A 활동량 탑 닉네임 조회 테스트")
    void getTopNicknamesTest() {
        // given
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"수학천재", 15L});
        mockResults.add(new Object[]{"피타고라스", 10L});
        mockResults.add(new Object[]{"오일러", 5L});

        when(qnaPostRepository.findTopNicknames(any(PageRequest.class))).thenReturn(mockResults);

        // when
        List<Map<String, Object>> rankings = qnaPostService.getTopNicknames();

        // then
        assertThat(rankings).hasSize(3);
        
        assertThat(rankings.get(0).get("nickName")).isEqualTo("수학천재");
        assertThat(rankings.get(0).get("postCount")).isEqualTo(15L);

        assertThat(rankings.get(1).get("nickName")).isEqualTo("피타고라스");
        assertThat(rankings.get(1).get("postCount")).isEqualTo(10L);

        assertThat(rankings.get(2).get("nickName")).isEqualTo("오일러");
        assertThat(rankings.get(2).get("postCount")).isEqualTo(5L);
    }
}
