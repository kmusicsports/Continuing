package com.example.continuing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDto {

	private String type; // "S": Success, "W": Warning, "E": Error, "I": Information
	private String text; // message
}
