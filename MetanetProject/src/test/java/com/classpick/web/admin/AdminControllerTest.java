package com.classpick.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.classpick.web.admin.controller.AdminController;
import com.classpick.web.admin.service.IAdminService;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.util.GetAuthenUser;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static MockedStatic<GetAuthenUser> mockedAuth;
    private final String adminUser = "admin";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        // ✅ GetAuthenUser Mocking 설정
        mockedAuth = Mockito.mockStatic(GetAuthenUser.class);
        mockedAuth.when(GetAuthenUser::getAuthenUser).thenReturn("admin");

        // ✅ SecurityContext 강제 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("Admin")));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        if (mockedAuth != null) {
            mockedAuth.close();
        }
    }

    @Test
    @DisplayName("회원 전체 조회 - 성공")
    void getAllMembers_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(adminService.getAllMembers()).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)) // ✅ 추가됨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("회원 삭제 - 성공")
    void deleteMembers_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(adminService.deleteMembers(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(delete("/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) // ✅ 추가됨
                        .content(objectMapper.writeValueAsString(Map.of("memberIds", List.of(1, 2, 3)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("전체 회원 삭제 - 성공")
    void deleteAllMembers_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(adminService.deleteAllMembers()).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(delete("/admin/accounts/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)) // ✅ 추가됨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("강의 삭제 - 성공")
    void deleteLectures_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(adminService.deleteLectures(any())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(delete("/admin/lectures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON) // ✅ 추가됨
                        .content(objectMapper.writeValueAsString(Map.of("lectureIds", List.of(10, 20, 30)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("전체 강의 삭제 - 성공")
    void deleteAllLectures_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(adminService.deleteAllLectures()).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(delete("/admin/lectures/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)) // ✅ 추가됨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }
}
