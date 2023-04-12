package com.vaadin.vbcteam5.data.service;

import java.util.List;

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
        return repository.save(question);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public int count() {
        return (int) repository.count();
    }

}
