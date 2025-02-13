package com.classpick.web.account;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.classpick.web.account.controller.AccountController;
import com.classpick.web.account.service.IAccountService;
import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.jwt.JwtTokenProvider;
import com.classpick.web.util.GetAuthenUser;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@Transactional
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAccountService accountService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AccountController accountController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String accessToken;
    private static MockedStatic<GetAuthenUser> mockedAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        accessToken = "Bearer mock-jwt-token";

        // ✅ GetAuthenUser.getAuthenUser()를 Mocking하여 항상 "asdf1234"를 반환하도록 설정
        mockedAuth = Mockito.mockStatic(GetAuthenUser.class);
        mockedAuth.when(GetAuthenUser::getAuthenUser).thenReturn("asdf1234");
    }

    @AfterEach
    void tearDown() {
        if (mockedAuth != null) {
            mockedAuth.close();
        }
    }

    @Test
    @DisplayName("✅ 내 수강 강의 목록 조회 - 성공")
    void getMyLecture_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.getLecture(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/account/lecture")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 관심 분야 등록 - 성공")
    void insertCategory_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.insertCategory(any(), any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(post("/account/category")
                        .param("tags", "AI,Cloud,Security")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 프로필 조회 - 성공")
    void getMypage_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.getMyPage(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/account")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 구매 내역 조회 - 성공")
    void getPaylog_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.getPaylog(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/account/pay-log")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 내 학습률 대시보드 조회 - 성공")
    void getMyStudy_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.getMyStudy(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/account/my-study")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 강사 강의 대시보드 조회 - 성공")
    void getTeacherLecture_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.getMyLecture(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/account/teacher-lecture")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 은행 정보 추가 - 성공")
    void addBank_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.addBank(any(), any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(post("/account/add-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bank", "KB Bank")))
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("✅ 은행 정보 삭제 - 성공")
    void deleteBank_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>(ResponseCode.SUCCESS, "SUCCESS", Map.of());
        Mockito.when(accountService.deleteBank(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(delete("/account/delete-bank")
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }
}
