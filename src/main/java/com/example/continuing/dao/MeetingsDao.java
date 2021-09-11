package com.example.continuing.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.continuing.entity.Meetings;
import com.example.continuing.form.SearchData;

public interface MeetingsDao {

	Page<Meetings> findByCriteria(SearchData searchData, Pageable pageable);
}
