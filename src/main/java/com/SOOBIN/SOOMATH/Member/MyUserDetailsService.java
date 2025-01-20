package com.SOOBIN.SOOMATH.Member;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private static final List<String> ADMIN_IDS = Arrays.asList("admin", "aleph_sbt");

    private final MemberRepository memberRepository;
    private final TempMemberRepository tempMemberRepository;

    public MyUserDetailsService(MemberRepository memberRepository, TempMemberRepository tempMemberRepository) {
        this.memberRepository = memberRepository;
        this.tempMemberRepository = tempMemberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var tempUser = tempMemberRepository.findByUsername(username);

        if (tempUser.isPresent()) {
            var user = tempUser.get();
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_VISITOR")
            );
            var a = new CustomUser(user.getUsername(), user.getPassword(), authorities);
            a.displayName = user.getDisplayName();
            return a;
        }

        var memberUser = memberRepository.findByUsername(username);
        if (memberUser.isPresent()) {
            var user = memberUser.get();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // 어드민 아이디 중에서 같은게 있는지 확인
            if (ADMIN_IDS.contains(user.getUsername())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            }
            var a = new CustomUser(user.getUsername(), user.getPassword(), authorities);
            a.displayName = user.getDisplayName();
            return a;
        }
        throw new UsernameNotFoundException("그런사람 또 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<TempMemberDTO> getAllTempMembers() {
        List<TempMember> tempMembers = tempMemberRepository.findAll();
        return tempMembers.stream()
                .map(tempMember -> new TempMemberDTO(
                        tempMember.getId(),
                        tempMember.getUsername(),
                        tempMember.getDisplayName(),
                        tempMember.getSchoolName(),
                        tempMember.getSeparated(),
                        tempMember.getGrade(),
                        tempMember.isAuth()))
                .collect(Collectors.toList());
    }

    @Getter
    @Setter
    public class TempMemberDTO {
        private final List<String> separated;
        private List<String> separatedNames;
        private Long id;
        private String username;
        private String displayName;
        private String schoolName;
        private int grade;
        private boolean auth;

        // 생성자
        public TempMemberDTO(Long id, String username, String displayName, String schoolName, List<String> separated, int grade, boolean auth) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.schoolName = schoolName;
            this.grade = grade;
            this.auth = auth;
            this.separated = separated;
        }
    }

        @Transactional(readOnly = true)
        public List<MemberDTO> getAllMembers() {
            List<Member> members = memberRepository.findAll();
            return members.stream()
                    .filter(member -> !ADMIN_IDS.contains(member.getUsername()))  // 관리자 제외
                    .map(member -> new MemberDTO(
                            member.getId(),
                            member.getUsername(),
                            member.getDisplayName(),
                            member.getSchoolName(),
                            member.getGrade(),
                            member.getSeparated(),
                            member.isAuth()))
                    .collect(Collectors.toList());
        }


    @Getter
    @Setter
    public class MemberDTO {
        private final List<String> separated; // 분반 코드 리스트
        private List<String> separatedNames; // 분반 이름 리스트
        private Long id;
        private String username;
        private String displayName;
        private String schoolName;
        private int grade;
        private boolean auth;

        // 생성자
        public MemberDTO(Long id, String username, String displayName, String schoolName, int grade, List<String> separated, boolean auth) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.schoolName = schoolName;
            this.grade = grade;
            this.separated = separated;
            this.auth = auth;
        }
    }

    public boolean isAdmin(String username) {
        return ADMIN_IDS.contains(username);
    }
}






