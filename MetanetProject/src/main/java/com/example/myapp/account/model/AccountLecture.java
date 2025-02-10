package com.example.myapp.account.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountLecture {
	//lectures의 category
	private String category;
	//lectures의 title
	private String title;
	//lectures의 profile
	private String profile;	
	//lectures의 finished
	private boolean finished;	
	//attends의 finished
	private boolean is_coursable;
}
