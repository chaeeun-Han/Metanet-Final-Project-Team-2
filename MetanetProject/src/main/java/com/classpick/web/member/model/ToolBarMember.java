package com.classpick.web.member.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolBarMember {
	private Member member;
	private List<String> category;
}
