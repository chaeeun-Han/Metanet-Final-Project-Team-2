package com.classpick.web.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.classpick.web.cart.dao.ICartRepository;
import com.classpick.web.cart.model.Cart;
import com.classpick.web.cart.service.CartService;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.member.dao.IMemberRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IMemberRepository memberRepository;

    @InjectMocks
    private CartService cartService;

    private Long testMemberId;
    private List<Cart> mockCartList;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;

        mockCartList = Arrays.asList(
            new Cart(1L, 100L, testMemberId, "Lecture 1", "Profile1", 5000, true, 50, new Date(System.currentTimeMillis()), 10),
            new Cart(2L, 200L, testMemberId, "Lecture 2", "Profile2", 10000, true, 100, new Date(System.currentTimeMillis()), 20)
        );
    }

    // ✅ 1. 장바구니 조회 - 성공
    @Test
    @DisplayName("장바구니 전체 조회 - 성공")
    void getCarts_Success() {
        when(memberRepository.getMemberIdById(anyString())).thenReturn(testMemberId);
        when(cartRepository.getCarts(anyLong())).thenReturn(mockCartList);

        ResponseEntity<ResponseDto> response = cartService.getCarts("testUser");

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(2, ((List<?>) response.getBody().getData()).size());
    }

    // ❌ 2. 장바구니 조회 - DB 오류 발생
    @Test
    @DisplayName("장바구니 전체 조회 - DB 오류")
    void getCarts_Fail_DBError() {
        when(memberRepository.getMemberIdById(anyString())).thenThrow(new RuntimeException("Database Error"));

        ResponseEntity<ResponseDto> response = cartService.getCarts("testUser");

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
    }

    // ✅ 3. 장바구니 추가 - 성공
    @Test
    @DisplayName("장바구니 추가 - 성공")
    void addCart_Success() {
        when(memberRepository.getMemberIdById(anyString())).thenReturn(testMemberId);
        doNothing().when(cartRepository).addCart(anyLong(), anyString());

        ResponseEntity<ResponseDto> response = cartService.addCart("testUser", "123");

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("success", response.getBody().getMessage());
    }

    // ❌ 4. 장바구니 추가 - DB 오류 발생
    @Test
    @DisplayName("장바구니 추가 - DB 오류")
    void addCart_Fail_DBError() {
        when(memberRepository.getMemberIdById(anyString())).thenThrow(new RuntimeException("Database Error"));

        ResponseEntity<ResponseDto> response = cartService.addCart("testUser", "123");

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
    }

    // ✅ 5. 장바구니 삭제 - 성공
    @Test
    @DisplayName("장바구니 삭제 - 성공")
    void deleteCarts_Success() {
        when(memberRepository.getMemberIdById(anyString())).thenReturn(testMemberId);
        when(cartRepository.getMemberIdbyCartId(anyLong())).thenReturn(testMemberId.toString());
        doNothing().when(cartRepository).deleteCart(anyLong(), anyLong());

        ResponseEntity<ResponseDto> response = cartService.deleteCarts("testUser", List.of(1L, 2L));

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("success", response.getBody().getMessage());
    }

    // ❌ 6. 장바구니 삭제 - 본인 아님
    @Test
    @DisplayName("장바구니 삭제 - 본인 아님")
    void deleteCarts_Fail_NotOwner() {
        when(memberRepository.getMemberIdById(anyString())).thenReturn(testMemberId);
        when(cartRepository.getMemberIdbyCartId(anyLong())).thenReturn("999"); // 다른 사용자 ID 반환

        ResponseEntity<ResponseDto> response = cartService.deleteCarts("testUser", List.of(1L));

        assertNotNull(response);
        assertEquals("CF", response.getBody().getCode()); // 인증 실패 코드 확인
    }

    // ❌ 7. 장바구니 삭제 - DB 오류 발생
    @Test
    @DisplayName("장바구니 삭제 - DB 오류")
    void deleteCarts_Fail_DBError() {
        when(memberRepository.getMemberIdById(anyString())).thenReturn(testMemberId);
        when(cartRepository.getMemberIdbyCartId(anyLong())).thenThrow(new RuntimeException("Database Error"));

        ResponseEntity<ResponseDto> response = cartService.deleteCarts("testUser", List.of(1L));

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
    }
}
