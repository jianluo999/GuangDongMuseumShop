package com.gdcp.guangdongmuseumshop.security;

import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityAuthServiceImpl implements SecurityAuthService {
    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication authenticate(LoginRequest loginRequest) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
    }
} 