package com.classpick.web.cart.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.classpick.web.cart.model.Cart;
import com.classpick.web.common.response.ResponseDto;

public interface ICartService {

	ResponseEntity<ResponseDto> getCarts(String memberId);
	ResponseEntity<ResponseDto> addCart(String memberId, String lecureId);
	ResponseEntity<ResponseDto> deleteCarts(String memberId, List<Long> cartId);

}
