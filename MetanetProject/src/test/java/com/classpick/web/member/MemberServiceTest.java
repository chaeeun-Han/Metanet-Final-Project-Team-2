package com.classpick.web.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import com.classpick.web.account.dao.IAccountRepository;
import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.jwt.JwtTokenProvider;
import com.classpick.web.jwt.model.JwtToken;
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
         
         Mockito.reset(memberRepository); 
     }

    @Test
    @DisplayName("멤버 추가 - 성공")
    void insertMember_ShouldCallRepository() {
        memberService.insertMember(member);
        verify(memberRepository, times(1)).insertMember(member);
    }

    @Test
    @DisplayName("아이디로 멤버 찾기 - 성공")
    void findById_ShouldReturnMember_IfExists() {
        when(memberRepository.findById("testUser")).thenReturn(Optional.of(member));
        assertTrue(memberService.findById("testUser").isPresent());
    }

    @Test
    @DisplayName("아이디로 멤버 추가 - 멤버 없음")
    void findById_ShouldReturnEmpty_IfNotExists() {
        when(memberRepository.findById("unknownUser")).thenReturn(Optional.empty());

        Optional<Member> found = memberService.findById("unknownUser");
        
        System.out.println("memberRepository.findById() 호출 확인: " + found.isPresent());

        assertFalse(found.isPresent());
        verify(memberRepository, times(1)).findById("unknownUser");
    }

    @Test
    @DisplayName("메일 전송 - 성공")
    void sendEmail_ShouldReturn200_IfSuccessful() throws MessagingException {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(redisUtil.existData(anyString())).thenReturn(false);
        assertEquals(200, memberService.sendEmail("join", "test@example.com").getStatusCode().value());
    }

    @Test
    @DisplayName("이메일 인증 코드 검사 - 성공")
    void verifyEmailCode_ShouldReturnCorrectStatus() {
        when(redisUtil.getData("test@example.com")).thenReturn("123456");
        assertEquals(200, memberService.verifyEmailCode("test@example.com", "123456").getStatusCode().value());
        assertEquals(401, memberService.verifyEmailCode("test@example.com", "654321").getStatusCode().value());
        when(redisUtil.getData("test@example.com")).thenReturn(null);
        assertEquals(400, memberService.verifyEmailCode("test@example.com", "123456").getStatusCode().value());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void loginService_ShouldReturnJwtToken() {
        Authentication authentication = mock(Authentication.class);
        JwtToken jwtToken = new JwtToken("Bearer", "accessToken", "refreshToken");

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtProvider.generateToken(authentication)).thenReturn(jwtToken);
        doNothing().when(redisTokenService).saveRefreshToken(anyString(), any());

        JwtToken result = memberService.loginService(member);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
    }

    @Test
    @DisplayName("같은 이메일 존재 여부")
    void isEmailDuplicated_ShouldReturnCorrectResult() {
        when(memberRepository.isEmailDuplicated("test@example.com")).thenReturn(1);
        assertTrue(memberService.isEmailDuplicated("test@example.com"));
        when(memberRepository.isEmailDuplicated("notfound@example.com")).thenReturn(0);
        assertFalse(memberService.isEmailDuplicated("notfound@example.com"));
    }

    @Test
    @DisplayName("리프레시 토큰 확인 - 성공")
    void checkRefreshToken_ShouldReturnCorrectResult() {
        when(jwtProvider.decodeRefreshToken("validToken")).thenReturn("user123");
        when(redisTokenService.existsRefreshToken("user123")).thenReturn(true);
        when(jwtProvider.generateTokenWithUserId("user123")).thenReturn(new JwtToken("Bearer", "newAccessToken", "newRefreshToken"));

        assertTrue(memberService.checkRefreshToken("validToken").contains("newAccessToken"));
        when(jwtProvider.decodeRefreshToken("invalidToken")).thenReturn("invalid token");
        assertEquals("invalid token", memberService.checkRefreshToken("invalidToken"));
    }

    @Test
    @DisplayName("이메일 초기화 - 성공")
    void resetEmail_ShouldReturnCorrectStatus() {
        when(memberRepository.getMemberIdById("testUser")).thenReturn(1L);
        doNothing().when(memberRepository).resetEmail("new@example.com", 1L);
        assertEquals(200, memberService.resetEmail("testUser", "new@example.com").getStatusCode().value());

        doThrow(new RuntimeException("DB Error")).when(memberRepository).resetEmail("new@example.com", 1L);
        assertEquals(500, memberService.resetEmail("testUser", "new@example.com").getStatusCode().value());
    }

    @Test
    @DisplayName("이메일 내용 초기화 - 성공")
    void setContext_ShouldReturnProcessedTemplate() throws Exception {
        List<String> mockData = List.of("Lecture 1", "Lecture 2");
        String result = invokePrivateMethod("setContext", mockData, "lecture/lecture_schedule_mail");
        assertTrue(result.contains("Lecture 1"));
    }

    private String invokePrivateMethod(String methodName, Object data, String templateName) throws Exception {
        Method method = MemberService.class.getDeclaredMethod(methodName, Object.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(memberService, data, templateName);
    }
    
    @Test
    @DisplayName("이메일 전송 - 레디스 오류")
    void sendEmail_RedisError_ShouldReturnRedisErrorResponse() throws MessagingException {

        MimeMessage mockMimeMessage = mock(MimeMessage.class);

        when(redisUtil.existData(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            System.out.println("existData() 호출됨: " + email);
            return true;
        });

        doThrow(new RuntimeException("Redis Error")).when(redisUtil).deleteData(anyString());

        ResponseEntity<ResponseDto> response = memberService.sendEmail("join", "test@example.com");

        verify(redisUtil, times(1)).existData(anyString());
        verify(redisUtil, times(1)).deleteData(anyString());

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("이메일 전송 - 전송 실패")
    void sendEmail_MailSendFail_ShouldReturnMailSendFailResponse() throws MessagingException {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        doAnswer(invocation -> { throw new MessagingException("Mail Send Error"); })
            .when(javaMailSender).send(any(MimeMessage.class));

        ResponseEntity<ResponseDto> response = memberService.sendEmail("join", "test@example.com");

        assertEquals(500, response.getStatusCode().value());
    }



    @Test
    @DisplayName("리프레시 토큰 확인 오류")
    void checkRefreshToken_Exception() {

        when(jwtProvider.decodeRefreshToken(anyString())).thenReturn("error");

        String result = memberService.checkRefreshToken("errorToken");

        assertEquals("error", result);
    }


    @Test
    @DisplayName("이메일 초기화 - 오류")
    void resetEmail_Exception() {
        when(memberRepository.getMemberIdById("testUser")).thenReturn(1L);
        doThrow(new RuntimeException("DB Error")).when(memberRepository).resetEmail("new@example.com", 1L);

        ResponseEntity<ResponseDto> response = memberService.resetEmail("testUser", "new@example.com");

        assertEquals(500, response.getStatusCode().value());
    }
    
    @Test
    @DisplayName("리프레시토큰 체크 - 만료")
    void checkRefreshToken_ShouldReturnExpire() {
        when(jwtProvider.decodeRefreshToken("expiredToken")).thenReturn("expire");
        assertEquals("expire", memberService.checkRefreshToken("expiredToken"));
    }

    @Test
    @DisplayName("리프레시토큰 체크 - 올바르지 않은 시그니처")
    void checkRefreshToken_ShouldReturnInvalidSignature() {
        when(jwtProvider.decodeRefreshToken("invalidSignatureToken")).thenReturn("invalid signature");
        assertEquals("invalid signature", memberService.checkRefreshToken("invalidSignatureToken"));
    }

    @Test
    @DisplayName("리프레시토큰 체크 - 올바르지 않은 토큰")
    void checkRefreshToken_ShouldReturnInvalidToken() {
        when(jwtProvider.decodeRefreshToken("invalidToken")).thenReturn("invalid token");
        assertEquals("invalid token", memberService.checkRefreshToken("invalidToken"));
    }

    @Test
    @DisplayName("리프레시토큰 체크 - 오류")
    void checkRefreshToken_ShouldReturnError() {
        when(jwtProvider.decodeRefreshToken("errorToken")).thenReturn("error");
        assertEquals("error", memberService.checkRefreshToken("errorToken"));
    }

    @Test
    @DisplayName("리프레시토큰 체크 - 새 토큰 발급")
    void checkRefreshToken_ShouldReturnNewTokens_IfValid() {
        String validToken = "validToken";
        String userId = "user123";
        JwtToken mockJwtToken = new JwtToken("Bearer", "newAccessToken", "newRefreshToken");

        when(jwtProvider.decodeRefreshToken(validToken)).thenReturn(userId);
        when(redisTokenService.existsRefreshToken(userId)).thenReturn(true);
        when(jwtProvider.generateTokenWithUserId(userId)).thenReturn(mockJwtToken);

        String result = memberService.checkRefreshToken(validToken);

        assertTrue(result.contains("newAccessToken"));
        assertTrue(result.contains("newRefreshToken"));
    }

    @Test
    @DisplayName("리프레시토큰 체크 - 레드스에 토큰 없음")
    void checkRefreshToken_ShouldReturnNotRedis_IfTokenNotFoundInRedis() {
        when(jwtProvider.decodeRefreshToken("validToken")).thenReturn("user123");
        when(redisTokenService.existsRefreshToken("user123")).thenReturn(false);

        assertEquals("not redis", memberService.checkRefreshToken("validToken"));
    }

    @Test
    @DisplayName("이메일폼 생성 - 회원가입")
    void createEmailForm_ShouldReturnCorrectSubject_ForJoinType() throws MessagingException {
        String email = "test@example.com";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        memberService.sendEmail("join", email);

        verify(javaMailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("이메일폼 생성 - 비밀번호 재설정")
    void createEmailForm_ShouldReturnCorrectSubject_ForPasswordType() throws MessagingException {
        String email = "test@example.com";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        memberService.sendEmail("password", email);

        verify(javaMailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("이메일폼 생성 - 이메일 확인")
    void createEmailForm_ShouldReturnCorrectSubject_ForEmailType() throws MessagingException {
        String email = "test@example.com";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        memberService.sendEmail("email", email);

        verify(javaMailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    @DisplayName("이메일폼 생성 - 강의 일정")
    void createEmailForm_ShouldReturnCorrectSubject_ForLectureScheduleType() throws MessagingException {
        String email = "test@example.com";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        memberService.sendEmail("lecture_schedule", email, "Lecture Data");

        verify(javaMailSender, times(1)).send(mockMimeMessage);
    }


    @Test
    @DisplayName("이메일폼 생성 - 메일 전송 실패")
    void createEmailForm_ShouldReturnMailSendFailResponse_ForInvalidType() throws MessagingException {
        String email = "test@example.com";

        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        ResponseEntity<ResponseDto> response = memberService.sendEmail("invalid_type", email);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ResponseCode.MAIL_FAIL, response.getBody().getCode());
        assertEquals(ResponseMessage.MAIL_FAIL, response.getBody().getMessage());
    }



}
