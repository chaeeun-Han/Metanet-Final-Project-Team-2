package com.classpick.web.account.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pay {
	private Long payId;
	private boolean status;
	private int price;
	private String startDate;
	private String endDate;
}
