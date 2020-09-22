import com.nask.task.models.Starship
import com.nask.task.services.Swapi
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class SwapiSpec extends Specification{

    public static Swapi swapi

    public static MockWebServer mockBackEnd

    def setupSpec() throws IOException {
        mockBackEnd = new MockWebServer()
        mockBackEnd.start()
        swapi = new Swapi(mockBackEnd.url("").toString())
    }

    def cleanupSpec() throws IOException {
        mockBackEnd.shutdown()
    }

    def "when swapi gets single non-existing person ResponseStatusException should be thrown"() {
        given:
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json"))

        when: "We request non-existing resource"
        def resource = swapi.getPerson(1).block()

        then:
        thrown(ResponseStatusException)
        resource == null
    }

    def "when swapi gets a single existing person it should return apropriate object with field set up"() {
        given:
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"))

        when:
        def resource = swapi.getPerson(1).block()

        then:
        resource.getName() == name

        where:
        body                        |       name
        "{\"name\": \"Jack\"}"      |       "Jack"
        "{\"name\": \"Jane\"}"      |       "Jane"
        "{\"name\": \"Yoda\"}"      |       "Yoda"
    }

    def "when swapi gets a single existing person it should assign it's id based on self url or null if url is broken"() {
        given:
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"))

        when:
        def resource = swapi.getPerson(id).block()

        then:
        resource.getName() == name
        resource.getId() == url_id

        where:
        body                                    |       name        |   id  | url_id
        "{\"name\": \"Jack\"," +
                " \"url\": \"url.com/1/\"}"     |       "Jack"      |   1   |   "1"
        "{\"name\": \"Jane\"," +
                " \"url\": \"url.com/10/\"}"    |       "Jane"      |   10  |   "10"
        "{\"name\": \"Yoda\"," +
                " \"url\": \"url.com\"}"        |       "Yoda"      |   15  |   null
        "{\"name\": \"Anakin\"," +
                " \"url\": \"\"}"               |       "Anakin"    |   15  |   null
        "{\"name\": \"Luke\"," +
                " \"url\": \"url.com/abc/\"}"   |       "Luke"      |   15  |   null
    }

    def "when swapi gets a single existing person it should request and add a homeworld and lisst of starships"() {
        given:
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bodyPerson)
                .addHeader("Content-Type", "application/json"))
        if (bodyPlanet != null) {
            mockBackEnd.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(bodyPlanet)
                    .addHeader("Content-Type", "application/json"))
        }
        bodyStarships.each {bodyStarship ->
            mockBackEnd.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(bodyStarship)
                    .addHeader("Content-Type", "application/json"))
        }

        when:
        def resource = swapi.getPerson(1).block()

        then:
        if(bodyPlanet == null) {
            resource.getHomeworld() == null
        } else {
            resource.getHomeworld().getName() == homeworldName
        }
        if(resource.getStarships()){
            resource.getStarships().eachWithIndex {starship, idx ->
                starship.getName() == starshipNames[idx]
            }
        }

        where:
        bodyPerson | bodyPlanet | bodyStarships | homeworldName | starshipNames
        "{\"homeworld\": \"validHomeworldUrl/1/\", \"starships\":[\"validStarshipUrl/1/\", \"validStarshipUrl/2/\"]}"
                | "{\"name\":\"Tatooine\"}"
                | ["{\"name\":\"X-Wing\"}", "{\"name\":\"Falcon\"}"]
                | "Tatooine"
                | ["X-Wing", "Falcon"]
        "{\"homeworld\": \"brokenHomeworldUrl\", \"starships\":[\"validStarshipUrl/1/\", \"validStarshipUrl/2/\"]}"
                | null
                | ["{\"name\":\"X-Wing\"}", "{\"name\":\"Falcon\"}"]
                | null
                | ["X-Wing", "Falcon"]
        "{\"homeworld\": \"validHomeworldUrl/1/\", \"starships\":[\"brokenFirstShipUrl\", \"validStarshipUrl/2/\"]}"
                | "{\"name\":\"Tatooine\"}"
                | ["{\"name\":\"Falcon\"}"]
                | "Tatooine"
                | ["Falcon"]
        "{\"homeworld\": \"validHomeworldUrl/1/\", \"starships\":[]}"
                | "{\"name\":\"Tatooine\"}"
                | []
                | "Tatooine"
                | []

    }

    def "when swapi gets a non-existing page it should throw ResponseStatusException and the value should be null"() {
        given:
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json"))

        when:
        def resource = swapi.generatePage(1).block()

        then:
        thrown(ResponseStatusException)
        resource == null

    }

    def "when swapi gets an existing page it should return a valid object with list of people and their specific information"() {
        given:
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/people/?page=1")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"count\":\"82\", \"results\":[{\"name\": \"Jack\"}," +
                            "{\"name\":\"Jay\", \"homeworld\":\"validHomeworldUrl/1/\"}," +
                            "{\"name\":\"Luke\", \"homeworld\":\"validHomeworldUrl/1/\", \"starships\": " +
                            "[\"http://swapi.dev/api/starships/12/\"," +
                            "\"http://swapi.dev/api/starships/22/\"]}]}")
                    .addHeader("Content-Type", "application/json")
                }
                if (request.getPath().contains("/planets/1/")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"name\":\"Tatooine\"}")
                            .addHeader("Content-Type", "application/json")
                }
                if (request.getPath().contains("/starships/12/")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"name\":\"X-Wing\"}")
                            .addHeader("Content-Type", "application/json")
                }
                if (request.getPath().contains("/starships/22/")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"name\":\"Falcon\"}")
                            .addHeader("Content-Type", "application/json")
                }
                return new MockResponse().setResponseCode(404);
            }
        }
        mockBackEnd.setDispatcher(mDispatcher);
        def starship12 = new Starship()
        starship12.setName("X-Wing")
        def starship22 = new Starship()
        starship22.setName("Falcon")

        when:
        def resource = swapi.generatePage(1).block()
        def people = resource.getElements()
        def p3Starships = people[2].getStarships()

        then:
        resource.getCount() == 82
        resource.getPages() == 9
        resource.getElements().size() == 3
        people[0].getName() == "Jack"
        people[1].getName() == "Jay"
        people[1].getHomeworld().getName() == "Tatooine"
        people[2].getName() == "Luke"
        people[2].getHomeworld().getName() == "Tatooine"
        people[2].getStarships().size() == 2
        p3Starships.stream().filter(starship -> {
            "X-Wing" == (starship.getName())
        }).count() == 1
        p3Starships.stream().filter(starship -> {
            "Falcon" == (starship.getName())
        }).count() == 1


        cleanup:
        mockBackEnd = new MockWebServer()
        mockBackEnd.start()
        swapi = new Swapi(mockBackEnd.url("").toString())

    }

}
