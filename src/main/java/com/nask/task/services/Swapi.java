package com.nask.task.services;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nask.task.models.Person;
import com.nask.task.models.Planet;
import com.nask.task.models.Starship;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class Swapi {

    private static final String LINK = "https://swapi.dev/api/";
    private static final String PEOPLE_RESOURCE = "people";
    private static final String PLANET_RESOURCE = "planets";
    private static final String STARSHIP_RESOURCE = "starships";

    @Autowired
    private RestTemplate restTemplate;

    @Async
    private <T> CompletableFuture<ResponseEntity<T>> getResource(String resource,Class<T> mapClass, int id)
            throws InterruptedException {
        String url = Swapi.LINK + resource + "/{id}/";
        return CompletableFuture.completedFuture(this.restTemplate.getForEntity(url, mapClass, id));
    }

    private <T> T getSpecific(String resource, Class<T> mapClass, int id) {
        T ret = null;
        try {
            ResponseEntity<T> response = this.getResource(resource, mapClass, id).get();
            if(response.getStatusCode() == HttpStatus.OK) {
                ret = response.getBody();
            } else {
                log.info(String.format("Resource %s with id %d not found", resource, id));
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
        return ret;
    }

    private Planet getPlanet(int id) {
        return this.getSpecific(Swapi.PLANET_RESOURCE, Planet.class, id);
    }

    private Starship getStarship(int id) {
        return this.getSpecific(Swapi.STARSHIP_RESOURCE,Starship.class, id);
    }

    /**
     * @param id - id of person to fetch
     * @return Person - object if requested person exists else null
     */
    public Person getPerson(int id) {
        Person ret = this.getSpecific(Swapi.PEOPLE_RESOURCE, Person.class, id);
        // If returned person is not null then fill additional fields.
        if (ret != null) {
            ret.setId(Integer.toString(id));
            Pattern p = Pattern.compile("/(\\d+)/\\z");
            Matcher m;
            String homeworldUrl = ret.getHomeworldUrl();
            if (homeworldUrl != null) {
                m = p.matcher(homeworldUrl);
                if (m.find()) {
                    ret.setHomeworld(getPlanet(Integer.parseInt(m.group(1))));
                }
            }
            String[] starshipsUrls = ret.getStarshipsUrls();
            if (starshipsUrls.length > 0) {
                Starship[] starships = new Starship[starshipsUrls.length];
                for (int i = 0; i < starshipsUrls.length; ++i) {
                    m = p.matcher(starshipsUrls[i]);
                    if (m.find()) {
                        starships[i] = getStarship(Integer.parseInt(m.group(1)));
                    }
                }
                ret.setStarships(starships);
            }
        }
        return ret;
    }

}
