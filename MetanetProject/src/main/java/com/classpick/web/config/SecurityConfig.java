package com.classpick.web.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.classpick.web.jwt.JwtAuthenticationFilter;
import com.classpick.web.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// Rest API이기 때문에 csrf 보안 사용 X
		http.csrf((csrfConfig) -> csrfConfig.disable());
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		// JWT를 사용하기 때문에 세션 사용 비활성
		http.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// 인가 규칙 설정
		http.authorizeHttpRequests(auth -> auth
			.requestMatchers("/api/css/**", "/api/js/**", "/api/images/**").permitAll()
			.requestMatchers("/api/public/**").permitAll()
			.requestMatchers("/api/auth/**").permitAll()
			.requestMatchers("/api/email/**").permitAll()
			.requestMatchers("/api/account/lecture", "/api/account/category", "/api/account/update", "/api/account", "/api/account/pay-log"
					,"/api/account/my-study").hasAnyRole("Student", "Teacher", "Admin")
			.requestMatchers("/api/account/teacher-lecture", "/api/account/edit-bank", "/api/account/delete-bank","/api/account/add-bank").hasAnyRole("Admin", "Teacher")
			.requestMatchers("/api/lectures/all", "/api/lectures/{lectureId:[0-9]+}", "/api/lectures/{lectureId:[0-9]+}/reviews").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/lectures/**", "/api/lectures/lectureLists/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/lectures/*/reviews").permitAll()
			.requestMatchers("/api/lectures/likes/**").hasAnyRole("Student", "Teacher", "Admin")
			.requestMatchers("/api/cart/**").permitAll()
			.requestMatchers("/api/certification/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/lectures/*/questions").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/lectures/*/questions/*").permitAll()
			.requestMatchers("/api/lectures/**").hasAnyRole("Student", "Teacher", "Admin")
			.requestMatchers("/api/account/revenue").hasAnyRole("Teacher", "Admin")
			.requestMatchers("/api/admin/**").hasRole("Admin")
			.requestMatchers("/ws/**").permitAll()
	        .requestMatchers("/api/user/**").permitAll()
	        .requestMatchers("/api/topic/**", "/queue/**").permitAll()
	        .requestMatchers("/api/zoom/*/meetings").hasAnyRole("Teacher", "Admin")
	        .requestMatchers("/api/zoom/*").permitAll()
	        .requestMatchers("/favicon.ico").permitAll()
	        .requestMatchers("/api/excel/**").hasAnyRole("Teacher", "Admin")
			  
			.anyRequest().authenticated() // 모든 요청은 인증이 필요
			).exceptionHandling(exception -> exception.accessDeniedPage("/api/access-denied"));

		// JWT 인증을 위해 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행
		http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build(); // 필터 체인 빌드
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // 비밀번호 암호화에 BCryptPasswordEncoder 사용
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration configuration = new CorsConfiguration();
	    configuration.setAllowedOriginPatterns(List.of("https://bamjun.click")); 
	    configuration.setExposedHeaders(List.of("Authorization"));
	    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "skipInterceptor", "Cache-Control"));
	    configuration.setAllowCredentials(true);

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    return source;
	}

}
