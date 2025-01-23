package com.SOOBIN.SOOMATH.QnaBoard;

import com.SOOBIN.SOOMATH.QnaBoard.QnaPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QnaPostService {
    private final QnaPostRepository qnaPostRepository;

    public List<Map<String, Object>> getTopNicknames() {
        List<Object[]> results = qnaPostRepository.findTopNicknames(PageRequest.of(0, 10));
        List<Map<String, Object>> rankings = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("nickName", result[0]); // 닉네임
            data.put("postCount", result[1]); // 글 개수
            rankings.add(data);
        }
        return rankings;
    }
}
