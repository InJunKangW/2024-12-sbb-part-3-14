package com.programmers.user;

import com.programmers.user.dto.SignupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteUserService {
    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser save(SignupDto signupDto) {
        if(siteUserRepository.existsByUsername(signupDto.userName())){

        }
        return siteUserRepository.save(SiteUser.builder()
                        .username(signupDto.userName())
                        .password(passwordEncoder.encode(signupDto.password()))
                        .email(signupDto.email())
                .build());
    }
}
