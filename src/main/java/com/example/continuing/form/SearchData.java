package com.example.continuing.form;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "topicList")
public class SearchData {

	private String keyword;
	private List<String> topicList;
    private int numberPeople;
    private String date;
    private String startTime;
    private String endTime;
    
    
    public SearchData() {
    	keyword = "";
    	topicList = new ArrayList<>();
    	numberPeople = 0;
    	date = "";
    	startTime = "";
    	endTime = "";
    }
}
