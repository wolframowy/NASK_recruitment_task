import com.nask.task.controllers.CharactersController
import com.nask.task.models.Page
import com.nask.task.models.Person
import com.nask.task.models.Planet
import com.nask.task.models.Starship
import com.nask.task.services.Swapi
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import spock.lang.Specification

@ContextConfiguration(classes = CharactersController.class)
@WebFluxTest(controllers = CharactersController.class)
class CharactersControllerSpec extends Specification {

    @Autowired
    private WebTestClient webTestClient;

    @SpringBean
    private Swapi swapi = Mock(Swapi)

    def "when get is performed for single character with broken id then exception should be thrown"() {
        expect:
        this.webTestClient.get()
                .uri("/characters/" + bad_id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()

        where:
        _ | bad_id
        _ | "abc"
        _ | "<2e"
        _ | "1e15"
        _ | "10.0"
        _ | "-5"
    }

    def "when non-existing character is requested return 404 with error message"() {
        given:
        swapi.getPerson(17) >> { _ -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error message") }

        expect:
        def body = this.webTestClient.get()
                .uri("/characters/17")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("\$.message").isEqualTo("Error message")
    }

    def "when existing character is requested return status code 200 and appropriate serialized json string"() {
        given:
        def homeworld = new Planet()
        homeworld.setName("Tatooine")
        def starship1 = new Starship()
        starship1.setName("X-Wing")
        def starship2 = new Starship()
        starship2.setName("Falcon")
        def person = new Person()
        person.setName("Jack")
        person.setHomeworld(homeworld)
        person.addStarship(starship1)
        person.addStarship(starship2)
        swapi.getPerson(1) >> Mono.just(person)

        expect:
        this.webTestClient.get()
                .uri("/characters/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("\$.name").isNotEmpty()
                .jsonPath("\$.name").isEqualTo("Jack")
                .jsonPath("\$.homeworld").isNotEmpty()
                .jsonPath("\$.homeworld.name").isEqualTo("Tatooine")
                .jsonPath("\$.starships").isNotEmpty()
                .jsonPath("\$.starships").isArray()
                .jsonPath("\$.starships[0].name").isEqualTo("X-Wing")
                .jsonPath("\$.starships[1].name").isEqualTo("Falcon")

    }

    def "when get is performed for page with broken number then BAD_REQUEST exception should be thrown"() {
        expect:
        this.webTestClient.get()
                .uri("/characters?page=" + bad_num)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()

        where:
        _ | bad_num
        _ | "abc"
        _ | "<2e"
        _ | "1e15"
        _ | "10.0"
        _ | "-5"
    }

    def "when get is performed for non-existing page then 404 exception should be thrown"() {
        given:
        swapi.generatePage(500) >> { _ -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error message") }

        expect:
        this.webTestClient.get()
                .uri("/characters?page=" + 500)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("\$.message").isEqualTo("Error message")

    }

    def "when get for characters page is performed without parameter it should default it to 1 and it should return Page object with appropriate values"() {
        given:
        def page = new Page()
        page.setCount(82)
        def homeworld = new Planet()
        homeworld.setName("Tatooine")
        def starship1 = new Starship()
        starship1.setName("X-Wing")
        def starship2 = new Starship()
        starship2.setName("Falcon")
        def person = new Person()
        person.setName("Jack")
        person.setHomeworld(homeworld)
        person.addStarship(starship1)
        person.addStarship(starship2)
        page.addElement(person)
        page.addElement(person)
        page.addElement(person)
        1 * swapi.generatePage(1) >> Mono.just(page)

        expect:
        this.webTestClient.get()
                .uri("/characters")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("\$.count").isEqualTo("82")
                .jsonPath("\$.pages").isEqualTo("9")
                .jsonPath("\$.elements").isArray()
                .jsonPath("\$.elements[2]").isNotEmpty()
                .jsonPath("\$.elements[2].name").isEqualTo("Jack")
                .jsonPath("\$.elements[2].homeworld").isNotEmpty()
                .jsonPath("\$.elements[2].homeworld.name").isEqualTo("Tatooine")
                .jsonPath("\$.elements[2].starships").isArray()
                .jsonPath("\$.elements[2].starships[0].name").isEqualTo("X-Wing")
                .jsonPath("\$.elements[2].starships[1].name").isEqualTo("Falcon")

    }

}