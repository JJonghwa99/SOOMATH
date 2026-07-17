package com.SOOBIN.SOOMATH.Member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TempMemberRepository tempMemberRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Test
    @DisplayName("가입 승인 대기자(TempMember) 로그인 시 ROLE_VISITOR 권한 부여 테스트")
    void loadUserByUsername_TempMemberTest() {
        // given
        String username = "tempuser";
        TempMember tempMember = new TempMember();
        tempMember.setUsername(username);
        tempMember.setPassword("password123");
        tempMember.setDisplayName("임시대기");
        tempMember.setAuth(false);

        when(tempMemberRepository.findByUsername(username)).thenReturn(Optional.of(tempMember));

        // when
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_VISITOR");
    }

    @Test
    @DisplayName("승인 완료된 일반 학생(Member) 로그인 시 ROLE_STUDENT 권한 부여 테스트")
    void loadUserByUsername_StudentMemberTest() {
        // given
        String username = "studentuser";
        Member member = new Member();
        member.setUsername(username);
        member.setPassword("password123");
        member.setDisplayName("학생회원");
        member.setAuth(true);

        when(tempMemberRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));

        // when
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_STUDENT");
    }

    @Test
    @DisplayName("어드민 계정(Member) 로그인 시 ROLE_ADMIN 권한 부여 테스트")
    void loadUserByUsername_AdminMemberTest() {
        // given
        String username = "admin";
        Member member = new Member();
        member.setUsername(username);
        member.setPassword("password123");
        member.setDisplayName("관리자");
        member.setAuth(true);

        when(tempMemberRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));

        // when
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("존재하지 않는 회원 로그인 시 예외 발생 테스트")
    void loadUserByUsername_NotFoundTest() {
        // given
        String username = "nonexistent";
        when(tempMemberRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(memberRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("그런사람 또 없습니다.");
    }

    @Test
    @DisplayName("어드민 아이디 여부 검증 테스트")
    void isAdminTest() {
        assertThat(myUserDetailsService.isAdmin("admin")).isTrue();
        assertThat(myUserDetailsService.isAdmin("aleph_sbt")).isTrue();
        assertThat(myUserDetailsService.isAdmin("studentuser")).isFalse();
    }
}
