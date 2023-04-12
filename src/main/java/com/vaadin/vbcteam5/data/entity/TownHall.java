package com.vaadin.vbcteam5.data.entity;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

@Entity
public class TownHall extends AbstractEntity {

    @NotNull
    private String name;

    @NotNull
    private LocalDateTime closeDate;

    @OneToMany(mappedBy = "townHall")
    @JsonIgnore
    private List<Question> questions = new LinkedList<>();

    public TownHall() {
    }

    public TownHall(String name, LocalDateTime closeDate) {
        this.name = name;
        this.closeDate = closeDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
