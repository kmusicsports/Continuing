package com.example.continuing.comparator;

import java.sql.Timestamp;
import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Meetings;

@Component
public class MeetingsComparator implements Comparator<Meetings> {

	@Override
    public int compare(Meetings meeting1, Meetings meeting2) {

		Timestamp timestamp1 = Utils.dateAndTime2Timestamp(meeting1.getDate(), meeting1.getStartTime());
		Timestamp timestamp2 = Utils.dateAndTime2Timestamp(meeting1.getDate(), meeting1.getStartTime());
        return timestamp1.compareTo(timestamp2);
    }
}
