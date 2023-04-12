package com.vaadin.vbcteam5.data.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vaadin.vbcteam5.data.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    List<Question> findAllByTownHall_IdOrderByRank(Long townHallId);

    List<Question> findAllByTownHall_IdAndAuthor_Id(Long townHallId, Long authorId);

}
