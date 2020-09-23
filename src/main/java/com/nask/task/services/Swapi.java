package com.nask.task.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.nask.task.models.Page;
import com.nask.task.models.Person;
import com.nask.task.models.Planet;
import com.nask.task.models.Starship;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@Slf4j
public class Swapi {

    private static final String PEOPLE_RESOURCE = "people";
    private static final String PLANET_RESOURCE = "planets";
    private static final String STARSHIP_RESOURCE = "starships";

    private final WebClient client;

    public Swapi(@Value("${swapi.url}") String swapiUrl){
        client = WebClient.create(swapiUrl);
    }

    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String errorMessage) {
            super(errorMessage);
        }
    }

    private void handleInvalidUrlException(InvalidUrlException e, Object object) {
        log.error(e.getMessage() + " on object of " + object.getClass());
    }


    private <T> Mono<T> getResource(String resource,Class<T> mapClass, int id) {
        return client.get()
                .uri(resource + "/{id}/", id)
                .retrieve()
                .bodyToMono(mapClass)
                .doOnError(ex -> {
                    String msg = String.format("Requested resource '%s' with id '%d' not found", resource, id);
                    log.error(msg);});
    }

    private Mono<Planet> getPlanet(int id) {
        return this.getResource(Swapi.PLANET_RESOURCE, Planet.class, id);
    }

    private Mono<Starship> getStarship(int id) {
        return this.getResource(Swapi.STARSHIP_RESOURCE,Starship.class, id);
    }

    private int getId(String url) throws Swapi.InvalidUrlException {
        Pattern p = Pattern.compile("/(\\d+)/\\z");
        if (url == null) {
            throw new Swapi.InvalidUrlException("Url is null");
        }
        Matcher m = p.matcher(url);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new Swapi.InvalidUrlException(String.format("%s url doesn't have a valid 'id' field", url));
    }

    private Mono<Person> fillPersonInfo(Mono<Person> personMono) {
        return personMono
                .flatMap(person -> {
                    try {
                        person.setId(Integer.toString(this.getId(person.getUrl())));
                    } catch (InvalidUrlException e) {
                        this.handleInvalidUrlException(e, person);
                    }
                    try {

                        int homeworldId = getId(person.getHomeworldUrl());
                        return getPlanet(homeworldId)
                                .doOnSuccess(person::setHomeworld).thenReturn(person);
                    } catch (InvalidUrlException e) {
                        this.handleInvalidUrlException(e, person);
                    }
                    return Mono.just(person);})
                .flatMap(person -> Flux.fromIterable(person.getStarshipsUrls())
                    .parallel(4).runOn(Schedulers.parallel())
                    .flatMap(s -> this.getStarship(this.getId(s)))
                    .doOnNext(person::addStarship)
                    .sequential()
                    .onErrorContinue(InvalidUrlException.class,
                        (e, v) -> this.handleInvalidUrlException((InvalidUrlException) e, v))
                    .then(Mono.just(person)));
    }

    /**
     * @param id - id of person to fetch
     * @return Mono<Person> - mono object
     */
    public Mono<Person> getPerson(int id) {
        Mono<Person> person = getResource(Swapi.PEOPLE_RESOURCE, Person.class, id).doOnError(WebClientResponseException.class,
                e -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
                })
                .onErrorStop();
        return fillPersonInfo(person);
    }

    /**
     * @param pageNum - number of requested page
     * @return Mono<Page>
     */
    public Mono<Page> generatePage(int pageNum) {
        return client.get()
                .uri(String.format("people/?page=%d", pageNum))
                .retrieve()
                .bodyToMono(Page.class)
                .doOnError(WebClientResponseException.class,
                        e -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
                        })
                .onErrorStop()
                .flatMap(page -> Flux.fromIterable(page.getElements())
                            .parallel(4).runOn(Schedulers.parallel())
                            .flatMap(person -> this.fillPersonInfo(Mono.just(person)))
                            .sequential()
                            .then(Mono.just(page)));
    }

}
