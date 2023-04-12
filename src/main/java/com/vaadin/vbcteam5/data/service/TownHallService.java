package com.vaadin.vbcteam5.data.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.vaadin.vbcteam5.data.entity.TownHall;

@Service
public class TownHallService {

    private final TownHallRepository repository;

    public TownHallService(TownHallRepository repository) {
        this.repository = repository;
    }

    public List<TownHall> list() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "closeDate"));
    }

    public TownHall save(TownHall townHall) {
        return repository.save(townHall);
    }

}
