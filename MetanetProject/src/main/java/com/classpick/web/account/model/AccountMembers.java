package com.classpick.web.account.model;

import java.util.List;

import com.classpick.web.member.model.Member;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AccountMembers {
	private Member members;
	private List<String> myCategory;
	private List<String> category;
}
