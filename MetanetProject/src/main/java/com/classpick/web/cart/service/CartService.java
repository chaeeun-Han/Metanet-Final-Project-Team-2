package com.classpick.web.cart.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.classpick.web.cart.dao.ICartRepository;
import com.classpick.web.cart.model.Cart;
import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.lecture.service.ILectureService;
import com.classpick.web.member.dao.IMemberRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CartService implements ICartService {

	@Autowired
	ICartRepository cartRepository;

	@Autowired
	IMemberRepository memberRepository;

	@Autowired
	ILectureService lectureService;

	// 장바구니 전체 조회 - 채은
	@Override
	public ResponseEntity<ResponseDto> getCarts(String memberId) {
		List<Cart> carts = null;
		try {
			Long memberUID = memberRepository.getMemberIdById(memberId);
			carts = cartRepository.getCarts(memberUID);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}
		ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, carts);
		return ResponseEntity.ok(responseBody);
	}

	// 장바구니 추가 - 채은
	@Override
	public ResponseEntity<ResponseDto> addCart(String memberId, String lectureId) {
		try {
			Long memberUID = memberRepository.getMemberIdById(memberId);
			Map<String, Long> params = new HashMap<String, Long>();
			params.put("memberId", memberUID);
			params.put("lectureId", Long.parseLong(lectureId));

			int is_buyed = lectureService.isAttend(params);
			if (is_buyed == 0) {
				cartRepository.addCart(memberUID, lectureId);
			} else {
				return ResponseDto.alreadyBuyed();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}
		return ResponseDto.success();
	}

	// 장바구니 삭제 - 채은
	@Override
	public ResponseEntity<ResponseDto> deleteCarts(String memberId, List<Long> cartIds) {
		try {
			Long memberUID = memberRepository.getMemberIdById(memberId);
			for (Long cartId : cartIds) {
				String dbMemberId = cartRepository.getMemberIdbyCartId(cartId);
				if (!memberUID.toString().equals(dbMemberId)) {
					return ResponseDto.certificateFail();
				}

				cartRepository.deleteCart(memberUID, cartId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}
		return ResponseDto.success();
	}

}
