package com.vaadin.vbcteam5.data.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vaadin.vbcteam5.data.entity.TownHall;

@Repository
public interface TownHallRepository extends JpaRepository<TownHall, Long>, JpaSpecificationExecutor<TownHall> {
}
