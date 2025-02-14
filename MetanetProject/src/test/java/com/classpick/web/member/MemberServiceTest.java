package com.classpick.web.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import com.classpick.web.account.dao.IAccountRepository;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.excel.model.MemberForExcel;
import com.classpick.web.jwt.JwtTokenProvider;
import com.classpick.web.jwt.model.JwtToken;
import com.classpick.web.jwt.model.RefreshToken;
import com.classpick.web.jwt.service.RedisTokenService;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.member.model.Member;
import com.classpick.web.member.service.MemberService;
import com.classpick.web.util.RedisUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtProvider;

    @Mock
    private RedisTokenService redisTokenService;

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private IAccountRepository accountRepository;

    @InjectMocks
    private MemberService memberService;
    
    @Mock
    private MimeMessage mimeMessage;

    private Member member;

    @BeforeEach
    void setUp() {
    	MockitoAnnotations.openMocks(this); 
    	memberService = new MemberService(
    	        javaMailSender,
    	        redisUtil,
    	        authenticationManagerBuilder,
    	        jwtProvider,
    	        redisTokenService,
    	        memberRepository,
    	        accountRepository
    	    );
 
        member = new Member();
        member.setId("testUser");
        member.setEmail("test@example.com");
        member.setPassword("password123");
        
        System.out.println("memberRepository: " + memberRepository);
        System.out.println("redisTokenService: " + redisTokenService);
        Mockito.reset(memberRepository); 
        
        
    }

    @Test
    void insertMember() {
        memberService.insertMember(member);
        verify(memberRepository, times(1)).insertMember(member);
    }

    @Test
    void findById_ShouldReturnMember_WhenMemberExists() {
        when(memberRepository.findById("testUser")).thenReturn(Optional.of(member));
        Optional<Member> found = memberService.findById("testUser");
        assertTrue(found.isPresent());
        assertEquals("testUser", found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenMemberNotExists() {
//        when(memberRepository.findById("unknownUser")).thenReturn(Optional.empty());
//        Optional<Member> found = memberService.findById("unknownUser");
//        assertFalse(found.isPresent());
//        verify(memberRepository, times(1)).findById("unknownUser");
    	Optional<Member> found = memberService.findById("unknownUser");

        assertFalse(found.isPresent());

        verify(memberRepository, times(1)).findById("unknownUser");
    }

    @Test
    void sendEmail_ShouldReturnSuccessResponse() throws MessagingException {
    	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(redisUtil.existData(anyString())).thenReturn(false);
        ResponseEntity<ResponseDto> response = memberService.sendEmail("join", "test@example.com");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void verifyEmailCode_ShouldReturnSuccess_WhenCodeMatches() {
        when(redisUtil.getData("test@example.com")).thenReturn("123456");
        ResponseEntity<ResponseDto> response = memberService.verifyEmailCode("test@example.com", "123456");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void verifyEmailCode_ShouldReturnFailure_WhenCodeDoesNotMatch() {
        when(redisUtil.getData("test@example.com")).thenReturn("123456");
        ResponseEntity<ResponseDto> response = memberService.verifyEmailCode("test@example.com", "654321");
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void loginService_ShouldReturnJwtToken() {
        Authentication authentication = mock(Authentication.class);
        JwtToken jwtToken = new JwtToken("Bearer", "accessToken", "refreshToken");

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtProvider.generateToken(authentication)).thenReturn(jwtToken);
        doNothing().when(redisTokenService).saveRefreshToken(anyString(), any(RefreshToken.class));

        JwtToken result = memberService.loginService(member);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
    }

    @Test
    void findByEmail_ShouldReturnTrue_WhenEmailExists() {
        when(memberRepository.findByEmail("test@example.com")).thenReturn(1);
        assertTrue(memberService.findByEmail("test@example.com"));
    }

    @Test
    void findByEmail_ShouldReturnFalse_WhenEmailNotExists() {
        when(memberRepository.findByEmail("notfound@example.com")).thenReturn(0);
        assertFalse(memberService.findByEmail("notfound@example.com"));
    }

    @Test
    void resetPw_ShouldCallRepositoryMethod() {
        memberService.resetPw("test@example.com", "newPassword");
        verify(memberRepository, times(1)).setNewPw("test@example.com", "newPassword");
    }

    @Test
    void checkRefreshToken_ShouldReturnNewToken_WhenValid() {
        String decodedResult = "user123";
        when(jwtProvider.decodeRefreshToken("validToken")).thenReturn(decodedResult);
        when(redisTokenService.existsRefreshToken(decodedResult)).thenReturn(true);

        JwtToken newTokens = new JwtToken("Bearer", "newAccessToken", "newRefreshToken");
        when(jwtProvider.generateTokenWithUserId(decodedResult)).thenReturn(newTokens);

        String result = memberService.checkRefreshToken("validToken");

        System.out.println("checkRefreshToken() 결과: " + result);

        assertTrue(result.contains("newAccessToken"));
        assertTrue(result.contains("newRefreshToken"));
    }


    @Test
    void revokeRefreshToken_ShouldReturnTrue_WhenValid() {
        when(jwtProvider.decodeRefreshToken("validToken")).thenReturn("user123");
        when(redisTokenService.existsRefreshToken("user123")).thenReturn(true);

        assertTrue(memberService.revokeRefreshToken("validToken"));
    }

    @Test
    void deleteMemberByToken_ShouldReturnTrue_WhenDeletedSuccessfully() {
        when(jwtProvider.decodeRefreshToken("validToken")).thenReturn("user123");
        doNothing().when(memberRepository).deleteMember("user123");
        doNothing().when(redisTokenService).deleteRefreshToken("user123");

        assertTrue(memberService.deleteMemberByToken("validToken"));
    }

    @Test
    void checkRefreshTokenValidity_ShouldReturnTrue_WhenValid() {
        when(jwtProvider.decodeRefreshToken("validToken")).thenReturn("user123");
        when(redisTokenService.existsRefreshToken("user123")).thenReturn(true);

        assertTrue(memberService.checkRefreshTokenValidity("validToken"));
    }

    @Test
    void resetEmail_ShouldReturnSuccessResponse() {
        when(memberRepository.getMemberIdById("testUser")).thenReturn(1L);
        doNothing().when(memberRepository).resetEmail("new@example.com", 1L);

        ResponseEntity<ResponseDto> response = memberService.resetEmail("testUser", "new@example.com");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getMembersByLecture_ShouldReturnList() {
    	assertNotNull(memberRepository);

        when(memberRepository.getMembersByLecture(1L)).thenReturn(List.of(new MemberForExcel()));

        List<MemberForExcel> result = memberService.getMembersByLecture(1L);
        
        assertFalse(result.isEmpty());
        
        verify(memberRepository, times(1)).getMembersByLecture(1L);
    }

    @Test
    void isEmailDuplicated_ShouldReturnTrue_WhenEmailExists() {
        when(memberRepository.isEmailDuplicated("test@example.com")).thenReturn(1);
        assertTrue(memberService.isEmailDuplicated("test@example.com"));
    }
}
