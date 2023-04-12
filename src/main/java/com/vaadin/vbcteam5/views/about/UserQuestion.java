package com.vaadin.vbcteam5.views.about;

import java.util.ArrayList;
import java.util.List;

public class UserQuestion implements Cloneable {
    String userId;
    String name;
    String question;
    List<String> voteIds = new ArrayList<>();

    public UserQuestion() {
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    String getUserId() {
        return userId;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setQuestion(String question) {
        this.question = question;
    }

    String getQuestion() {
        return question;
    }

    void addVoteName(String id) {
        this.voteIds.add(id);
    }

    void setVoteIds(List<String> voteIds) {
        this.voteIds = voteIds;
    }

    List<String> getVoteIds() {
        return voteIds;
    }
}
