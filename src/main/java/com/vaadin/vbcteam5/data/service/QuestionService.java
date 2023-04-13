package com.vaadin.vbcteam5.data.service;

import java.util.List;

import com.github.pravin.raha.lexorank4j.LexoRank;
import org.springframework.stereotype.Service;

import com.vaadin.vbcteam5.data.entity.Question;

@Service
public class QuestionService {

    private final QuestionRepository repository;

    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
    }

    public List<Question> listByTownHall(long townHallId) {
        return repository.findAllByTownHall_IdOrderByRank(townHallId);
    }

    public List<Question> listByTownHallAndUser(Long townHallId, Long userId) {
        return repository.findAllByTownHall_IdAndAuthor_Id(townHallId, userId);
    }

    public Question update(Question question) {
        if (question.getId() == null) {
            Question lastRankedQuestion = repository.findFirstByTownHall_IdOrderByRankDesc(
                question.getTownHall().getId());
            LexoRank rank = LexoRank.min();
            if (lastRankedQuestion != null) {
                rank = LexoRank.parse((lastRankedQuestion.getRank())).genNext();
            }
            question.setRank(rank.toString());
        }
        return repository.save(question);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public int count() {
        return (int) repository.count();
    }

}
