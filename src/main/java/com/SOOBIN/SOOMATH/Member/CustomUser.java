package com.SOOBIN.SOOMATH.Member;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
class CustomUser extends User {
    public String displayName;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
    //displayName 받아오기위해서 수정 ㅅㅂ th 문법쓰면 됐는데 이거 때문에 ㅅㅃ
}
