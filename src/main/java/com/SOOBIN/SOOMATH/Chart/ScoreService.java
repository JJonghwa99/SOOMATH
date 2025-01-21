package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.Member;
import com.SOOBIN.SOOMATH.Member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ScoreService {

    private final MemberRepository memberRepository;
    private final ScoreRepository scoreRepository;

    public ScoreService(MemberRepository memberRepository, ScoreRepository scoreRepository) {
        this.memberRepository = memberRepository;
        this.scoreRepository = scoreRepository;
    }

    public void parseAndSaveScores(MultipartFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        String month = "11"; // 기본값 예: "11월"
        String separated = null;
        String[] exams = null;

        while ((line = reader.readLine()) != null) {
            String[] data = line.split("\t");

            // 줄이 학년/반/월 정보로 시작하는지 확인
            if (data[0].matches("\\d+-\\d+\\(\\d+\\)")) {
                exams = Arrays.stream(data, 1, data.length)
                        .filter(s -> !s.trim().isEmpty()) // 빈 문자열 제거
                        .toArray(String[]::new);
                if (exams.length == 0) {
                    System.out.println("No exam information found: " + line);
                    continue;
                }
                separated = data[0].substring(0, data[0].indexOf('('));
                month = data[0].substring(data[0].indexOf('(') + 1, data[0].indexOf(')'));
                continue;
            }


            // 실제 성적 데이터를 처리
            String username = data[0];
            Optional<Member> memberOpt = memberRepository.findByUsername(username);

            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();

                // 각 주차별 점수 저장
                for (int i = 1; i < data.length; i++) {
                    try {
                        Score score = new Score();
                        score.setMember(member);
                        score.setExam(exams[i - 1].trim()); // 주차 및 회차 정보 (예: "1.1")
                        score.setScore(Integer.parseInt(data[i].trim()));  // 점수 값
                        score.setSeparated(separated);
                        score.setMonth(month);  // 월 정보 설정
                        scoreRepository.save(score);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        System.out.println("Invalid data format for username " + username + ": " + line);
                    }
                }
            }
        }
    }

    public Map<String, Object> getStudentScores(String username) {
        // 현재 학생의 점수 데이터 가져오기
        List<Score> studentScores = scoreRepository.findByMemberUsername(username);
        if (studentScores.isEmpty()) {
            return new HashMap<>();
        }

        // 현재 학생의 첫 번째 점수에서 month와 separated 값을 가져옴
        String month = studentScores.get(0).getMonth();
        String separated = studentScores.get(0).getSeparated();

        // 같은 month와 separated를 가진 모든 점수 가져오기
        List<Score> allScores = scoreRepository.findByMonthAndSeparated(month, separated);

        // 시험별로 점수 분류
        Map<String, List<Integer>> examScoresMap = new TreeMap<>();
        Map<String, Integer> currentStudentScoresMap = new TreeMap<>();

        // 모든 점수를 시험별로 분류
        for (Score score : allScores) {
            String exam = score.getExam();
            examScoresMap.computeIfAbsent(exam, k -> new ArrayList<>())
                    .add(score.getScore());

            // 현재 학생의 점수 따로 저장
            if (score.getMember().getUsername().equals(username)) {
                currentStudentScoresMap.put(exam, score.getScore());
            }
        }

        // 결과 데이터 준비
        List<String> labels = new ArrayList<>(examScoresMap.keySet());
        List<Integer> minScores = new ArrayList<>();
        List<Integer> avgScores = new ArrayList<>();
        List<Integer> maxScores = new ArrayList<>();
        List<Integer> studentScoresList = new ArrayList<>();

        // 각 시험별 통계 계산
        for (String exam : labels) {
            List<Integer> scores = examScoresMap.get(exam);
            minScores.add(Collections.min(scores));
            maxScores.add(Collections.max(scores));

            // 평균 계산
            double avg = scores.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            avgScores.add((int) Math.round(avg));

            // 현재 학생의 점수 추가
            studentScoresList.add(currentStudentScoresMap.getOrDefault(exam, 0));
        }

        // 결과 맵 생성
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("minScores", minScores);
        result.put("avgScores", avgScores);
        result.put("maxScores", maxScores);
        result.put("studentScores", studentScoresList);
        result.put("month", month);  // 월 정보 추가
        result.put("separated", separated);  // 반 정보 추가

        return result;
    }

    public Map<String, Object> getScoresByCodeAndMonth(String username, String code, String month) {
        // 현재 학생의 점수 데이터 가져오기
        List<Score> studentScores = scoreRepository.findByMemberUsernameAndSeparatedAndMonth(username, code, month);

        if (studentScores.isEmpty()) {
            return new HashMap<>();
        }

        // 시험별로 점수 분류
        Map<String, List<Integer>> examScoresMap = new TreeMap<>();
        Map<String, Integer> currentStudentScoresMap = new TreeMap<>();

        for (Score score : studentScores) {
            String exam = score.getExam();
            examScoresMap.computeIfAbsent(exam, k -> new ArrayList<>()).add(score.getScore());

            // 현재 학생의 점수 따로 저장
            currentStudentScoresMap.put(exam, score.getScore());
        }

        // 결과 데이터 준비
        List<String> labels = new ArrayList<>(examScoresMap.keySet());
        List<Integer> minScores = new ArrayList<>();
        List<Integer> avgScores = new ArrayList<>();
        List<Integer> maxScores = new ArrayList<>();
        List<Integer> studentScoresList = new ArrayList<>();

        for (String exam : labels) {
            List<Integer> scores = examScoresMap.get(exam);
            minScores.add(Collections.min(scores));
            maxScores.add(Collections.max(scores));
            avgScores.add((int) Math.round(scores.stream().mapToInt(Integer::intValue).average().orElse(0.0)));
            studentScoresList.add(currentStudentScoresMap.getOrDefault(exam, 0));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("minScores", minScores);
        result.put("avgScores", avgScores);
        result.put("maxScores", maxScores);
        result.put("studentScores", studentScoresList);

        return result;
    }

}

