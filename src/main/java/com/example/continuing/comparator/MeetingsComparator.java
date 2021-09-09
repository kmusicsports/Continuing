package com.example.continuing.comparator;

import java.util.Comparator;

import com.example.continuing.entity.Meetings;

public class MeetingsComparator implements Comparator<Meetings> {

	@Override
    public int compare(Meetings meeting1, Meetings meeting2) {
        return meeting1.getDate().compareTo(meeting2.getDate());
    }
}
