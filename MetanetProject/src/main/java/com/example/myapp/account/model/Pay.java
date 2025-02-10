package com.example.myapp.account.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pay {
	private Long pay_id;
	private boolean status;
	private int price;
	private String start_date;
	private String end_date;
}
