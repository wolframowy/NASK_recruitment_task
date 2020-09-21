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

@RestController
@Slf4j
@RequestMapping("characters")
public class CharactersController {

    @Autowired
    private Swapi swapi;


    @GetMapping(path = "", produces = "application/json")
    public ResponseEntity<Mono<Page>> getPage(@RequestParam(value = "page", defaultValue = "1") int pageNum) {
        Mono<Page> page = swapi.generatePage(pageNum);
//        if (page.getElements().size() == 0) {
//            String msg = String.format("Requested page %d doesn't have any elements!", pageNum);
//            log.info(msg);
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
//        }
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Mono<Person>> getCharacter(@PathVariable int id) {
        Mono<Person> person = swapi.getPerson(id);
        HttpStatus status = person != null ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(person, status);
//        if (person == null) {
//            String msg = String.format("Requested person with id %d not found", id);
//            log.info(msg);
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
//        }
//        return new ResponseEntity<>(person, HttpStatus.OK);
    }

}
