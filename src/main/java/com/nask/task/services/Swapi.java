package com.nask.task.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nask.task.models.Page;
import com.nask.task.models.Person;
import com.nask.task.models.Planet;
import com.nask.task.models.Starship;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@Slf4j
public class Swapi {

    private static final String LINK = "https://swapi.dev/api/";
    private static final String PEOPLE_RESOURCE = "people";
    private static final String PLANET_RESOURCE = "planets";
    private static final String STARSHIP_RESOURCE = "starships";

    @Autowired
    private RestTemplate restTemplate;

    private final WebClient client = WebClient.create(Swapi.LINK);

    // TODO: Add error handling!!!
    private <T> Mono<T> getResource(String resource,Class<T> mapClass, int id) {
        return client.get()
                .uri(resource + "/{id}/", id)
                .retrieve()
                .bodyToMono(mapClass);
    }

    private Mono<Planet> getPlanet(int id) {
        return this.getResource(Swapi.PLANET_RESOURCE, Planet.class, id);
    }

    private Mono<Starship> getStarship(int id) {
        return this.getResource(Swapi.STARSHIP_RESOURCE,Starship.class, id);
    }

    /**
     * @param id - id of person to fetch
     * @return Mono<Person> - mono object
     */
    public Mono<Person> getPerson(int id) {
        final Pattern p = Pattern.compile("/(\\d+)/\\z");
        final Matcher[] m = new Matcher[1];
        return this.getResource(Swapi.PEOPLE_RESOURCE, Person.class, id)
                .doOnError(ex -> {
                    String msg = String.format("Requested person with id %d not found", id);
                    log.info(msg);})
                .flatMap(person -> {
                    person.setId(Integer.toString(id));
                    String homeworldUrl = person.getHomeworldUrl();
                    if (homeworldUrl != null) {
                        m[0] = p.matcher(homeworldUrl);
                        if (m[0].find()) {
                            return getPlanet(Integer.parseInt(m[0].group(1)))
                                    .doOnSuccess(person::setHomeworld).thenReturn(person);
                        }
                    }
                    return Mono.just(person);})
                .flatMap(person -> {
                    String[] starshipsUrls = person.getStarshipsUrls();
                    List<Mono<Starship>> mList = new ArrayList<>();
                    if (starshipsUrls.length > 0) {
                        for (String starshipsUrl : starshipsUrls) {
                            m[0] = p.matcher(starshipsUrl);
                            if (m[0].find()) {
                                mList.add(getStarship(Integer.parseInt(m[0].group(1))));
                            }
                        }
                    }
                    return Flux.concat(mList).parallel(4).runOn(Schedulers.parallel())
                            .doOnNext(person::addStarship).then().thenReturn(person);
                });
    }

    /**
     * @param pageNum - number of requested page
     * @return Mono<Page>
     */
    public Mono<Page> generatePage(int pageNum) {
        Page page = new Page();
        List<Mono<Person>> pList = new ArrayList<>();
        int elPerPage = page.getElementsPerPage();
        int start = Math.max(1, (pageNum - 1) * elPerPage + 1);
        for(int i = start; i < start + elPerPage; ++i) {
            pList.add(getPerson(i));
        }
        return Flux.concat(pList).parallel(4).runOn(Schedulers.parallel())
                .doOnNext(page::addElement).then().thenReturn(page);
    }

}
