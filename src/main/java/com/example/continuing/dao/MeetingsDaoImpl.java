package com.example.continuing.dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Meetings_;
import com.example.continuing.form.SearchData;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MeetingsDaoImpl implements MeetingsDao {

	private final EntityManager entityManager;
	
	@Override
	public Page<Meetings> findByCriteria(SearchData searchData, Pageable pageable) {
		

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Meetings> query = builder.createQuery(Meetings.class);
        Root<Meetings> root = query.from(Meetings.class);
        List<Predicate> predicates = new ArrayList<>();
        
        // keyword
        String keyword;
        if(searchData.getKeyword().length() > 0) {
        	keyword = "%" + searchData.getKeyword() + "%";
        } else {
        	keyword = "%";
        }
        predicates.add(builder.like(root.get(Meetings_.AGENDA), keyword));
        
        // topic
        if (searchData.getTopicList().size() != 0) {
        	predicates
        		.add(builder
        			.and((root.get(Meetings_.TOPIC)).in(searchData.getTopicList())));
        }
        
        // numberPeople
        if (searchData.getNumberPeople() > 0) {
            predicates
                .add(builder
                    .and(builder
                        .equal(root.get(Meetings_.NUMBER_PEOPLE),
                            searchData.getNumberPeople())));
        }
        
        // date
        if (!searchData.getDate().equals("")) {
            predicates
                .add(builder
                    .and(builder
                        .equal(root.get(Meetings_.DATE),
                            Utils.strToDate(searchData.getDate().replace("/", "-")))));
        } else {
        	predicates
            .add(builder
                .and(builder
                    .greaterThanOrEqualTo(root.get(Meetings_.DATE), new Date(System.currentTimeMillis()))));
        }
        
        // startTime
        if (!searchData.getStartTime().equals("")) {
            predicates
                .add(builder
                    .and(builder
                        .greaterThanOrEqualTo(root.get(Meetings_.START_TIME),
                            Utils.strToTime(searchData.getStartTime()))));
        }
        
        // endTime
        if (!searchData.getEndTime().equals("")) {
            predicates
                .add(builder
                    .and(builder
                        .lessThanOrEqualTo(root.get(Meetings_.END_TIME),
                            Utils.strToTime(searchData.getEndTime()))));
        }
        
        // SELECT作成
        Predicate[] predArray = new Predicate[predicates.size()];
        predicates.toArray(predArray);
        query = query.select(root).where(predArray).orderBy(builder.asc(root.get(Meetings_.id)));
        
        // クエリー生成
        TypedQuery<Meetings> typedQuery = entityManager.createQuery(query);
        // 総レコード数取得設定
        int totalRows = typedQuery.getResultList().size();
        // 先頭レコードの位置設定
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        // １ページあたりの件数
        typedQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
	}

}
