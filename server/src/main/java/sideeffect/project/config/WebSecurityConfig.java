package sideeffect.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sideeffect.project.security.*;
import sideeffect.project.security.oauth.Oauth2AuthenticationManager;
import sideeffect.project.security.oauth.Oauth2LoginFilter;
import sideeffect.project.service.OauthService;

@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig{

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final OauthService oauthService;


    @Bean
    public Oauth2AuthenticationManager oauth2AuthenticationManager() {
        return new Oauth2AuthenticationManager(oauthService);
    }

    @Bean
    public Oauth2LoginFilter oauth2LoginFilter() {
        Oauth2LoginFilter oauth2LoginFilter = new Oauth2LoginFilter();
        oauth2LoginFilter.setFilterProcessesUrl("/api/social/login");
        oauth2LoginFilter.setAuthenticationManager(oauth2AuthenticationManager());
        oauth2LoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler(refreshTokenProvider));
        return oauth2LoginFilter;
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler(RefreshTokenProvider refreshTokenProvider) {
        return new LoginSuccessHandler(refreshTokenProvider);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .httpBasic().disable()
                .csrf().disable()
                .cors().and()
                .authorizeRequests()
                .antMatchers("/api/user/join", "/api/user/mypage/**", "/api/user/duple/**", "/api/social/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/token/at-issue/**").permitAll()
            .antMatchers(HttpMethod.POST, "/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/free-boards/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/like/**").hasAnyRole("USER", "ADMIN")
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(oauth2LoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new SecurityExceptionHandlerFilter(), JwtFilter.class)
                .formLogin()
                    .loginProcessingUrl("/api/user/login")
                    .usernameParameter("email")
                    .successHandler(loginSuccessHandler(refreshTokenProvider))
                    .failureHandler(loginFailureHandler())
                    .and()
                .build();
    }
}
