package com.vaadin.vbcteam5.data.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Question extends AbstractEntity {

    private String text;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User author;

    @ManyToOne(optional = false)
    @JoinColumn(name = "town_hall_id")
    private TownHall townHall;

    @ManyToMany
    @JoinTable(name = "question_upvotes")
    private Set<User> upvotes = new HashSet<>();

    public Question() {
    }

    public Question(String text, User author, TownHall townHall) {
        this.text = text;
        this.author = author;
        this.townHall = townHall;
    }

    public String getText() {
        return text;
    }

    public void setText(String value) {
        this.text = value;
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public void setTownHall(TownHall townHall) {
        this.townHall = townHall;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Set<User> getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Set<User> upvotes) {
        this.upvotes = upvotes;
    }
}
