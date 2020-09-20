package com.nask.task.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("characters")
public class CharactersController {

    @GetMapping(path = "", produces = "application/json")
    public String getPage(@RequestParam(value = "page", defaultValue = "1") int page) {
        return String.format("Requested page %d", page);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public String getCharacter(@PathVariable int id) {
        return String.format("Requested %d", id);
    }

}
