package com.vaadin.vbcteam5.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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

    // TODO missing upvotes

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
}
