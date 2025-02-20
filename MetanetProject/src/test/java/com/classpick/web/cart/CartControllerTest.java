package com.classpick.web.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.classpick.web.cart.controller.CartController;
import com.classpick.web.cart.service.ICartService;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.util.GetAuthenUser;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICartService cartService;

    @InjectMocks
    private CartController cartController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static MockedStatic<GetAuthenUser> mockedAuth;
    private final String testUser = "testUser";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();

        // ✅ 사용자 인증 Mocking
        mockedAuth = Mockito.mockStatic(GetAuthenUser.class);
        mockedAuth.when(GetAuthenUser::getAuthenUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        if (mockedAuth != null) {
            mockedAuth.close();
        }
    }

    // ✅ 1. 장바구니 조회 - 성공
    @Test
    @DisplayName("장바구니 조회 - 성공")
    void getCarts_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", List.of());
        when(cartService.getCarts(anyString())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    // ✅ 2. 장바구니 조회 - 인증되지 않은 사용자
    @Test
    @DisplayName("장바구니 조회 - 인증 실패")
    void getCarts_Fail_Auth() throws Exception {
        mockedAuth.when(GetAuthenUser::getAuthenUser).thenReturn(null);

        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ✅ 3. 장바구니 조회 - DB 오류 발생 (Mocking으로 ResponseDto 반환)
    @Test
    @DisplayName("장바구니 조회 - DB 오류")
    void getCarts_Fail_DBError() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("DBE", "database error", null);
        when(cartService.getCarts(anyString())).thenReturn(ResponseEntity.status(500).body(mockResponse));

        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DBE"))
                .andExpect(jsonPath("$.message").value("database error"));
    }

    // ✅ 4. 장바구니 추가 - 성공
    @Test
    @DisplayName("장바구니 추가 - 성공")
    void addCart_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(cartService.addCart(anyString(), anyString())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(post("/cart")
                        .param("lectureId", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    // ✅ 5. 장바구니 추가 - 인증되지 않은 사용자
    @Test
    @DisplayName("장바구니 추가 - 인증 실패")
    void addCart_Fail_Auth() throws Exception {
        mockedAuth.when(GetAuthenUser::getAuthenUser).thenReturn(null);

        mockMvc.perform(post("/cart")
                        .param("lectureId", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ✅ 6. 장바구니 삭제 - 성공
    @Test
    @DisplayName("장바구니 삭제 - 성공")
    void deleteCarts_Success() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("SU", "SUCCESS", null);
        when(cartService.deleteCarts(anyString(), anyList())).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(delete("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartIds", List.of(1L, 2L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    // ✅ 7. 장바구니 삭제 - 인증되지 않은 사용자
    @Test
    @DisplayName("장바구니 삭제 - 인증 실패")
    void deleteCarts_Fail_Auth() throws Exception {
        mockedAuth.when(GetAuthenUser::getAuthenUser).thenReturn(null);

        mockMvc.perform(delete("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartIds", List.of(1L, 2L)))))
                .andExpect(status().isUnauthorized());
    }

    // ✅ 8. 장바구니 삭제 - 본인이 아닌 경우
    @Test
    @DisplayName("장바구니 삭제 - 본인 아님")
    void deleteCarts_Fail_NotOwner() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("CF", "CERTIFICATE FAIL", null);
        when(cartService.deleteCarts(anyString(), anyList())).thenReturn(ResponseEntity.status(401).body(mockResponse));

        mockMvc.perform(delete("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartIds", List.of(1L, 2L)))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CF"))
                .andExpect(jsonPath("$.message").value("CERTIFICATE FAIL"));
    }

    // ✅ 9. 장바구니 삭제 - DB 오류 발생
    @Test
    @DisplayName("장바구니 삭제 - DB 오류")
    void deleteCarts_Fail_DBError() throws Exception {
        ResponseDto<?> mockResponse = new ResponseDto<>("DBE", "database error", null);
        when(cartService.deleteCarts(anyString(), anyList())).thenReturn(ResponseEntity.status(500).body(mockResponse));

        mockMvc.perform(delete("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartIds", List.of(1L, 2L)))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DBE"))
                .andExpect(jsonPath("$.message").value("database error"));
    }
}
