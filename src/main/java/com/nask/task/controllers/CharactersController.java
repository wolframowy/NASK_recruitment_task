package com.nask.task.controllers;

import com.nask.task.models.Page;
import com.nask.task.models.Person;
import com.nask.task.services.Swapi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("characters")
public class CharactersController {

    @Autowired
    private Swapi swapi;

    @GetMapping(path = "", produces = "application/json")
    public ResponseEntity<Mono<Page>> getPage(@RequestParam(value = "page", defaultValue = "1") int pageNum) {
        Mono<Page> page = swapi.generatePage(pageNum);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Mono<Person>> getCharacter(@PathVariable int id) {
        Mono<Person> person = swapi.getPerson(id);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

}
