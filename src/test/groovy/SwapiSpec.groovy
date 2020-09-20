import com.nask.task.TaskApplication
import com.nask.task.models.Person
import com.nask.task.models.Planet
import com.nask.task.models.Starship
import com.nask.task.services.Swapi
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification


@ContextConfiguration(classes= TaskApplication.class)
class SwapiSpec extends Specification{

    @SpringBean
    RestTemplate restTemplate = Mock()

    @Autowired
    Swapi swapi

    def "when swapi gets non-existing resource returning value is null"() {
        when: "We request non-existing resource"
        def resource = this.swapi.getPerson(1)

        then:
        1 * restTemplate.getForEntity("https://swapi.dev/api/people/{id}/", Person.class, 1)
                >> ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        resource == null
    }

    def "when swapi gets an existing person it should return apropriate object and it should request for its starships and a homeworld"() {
        given:
        Person person = new Person()
        person.setName("Jack")
        person.setHomeworldUrl("url.com/5/")
        person.setStarshipsUrls(new String[] {"url.com/1/", "url.com/2/"})

        Planet homeworld = new Planet()
        homeworld.setName("Homeworld")

        Starship starship1 = new Starship()
        starship1.setName("s1")
        Starship starship2 = new Starship()
        starship2.setName("s2")

        when: "We request an existing resource"
        def resource = this.swapi.getPerson(1)

        then:
        1 * restTemplate.getForEntity("https://swapi.dev/api/people/{id}/", Person.class, 1)
                >> ResponseEntity.status(HttpStatus.OK).body(person)
        1 * restTemplate.getForEntity("https://swapi.dev/api/planets/{id}/", Planet.class, 5)
                >> ResponseEntity.status(HttpStatus.OK).body(homeworld)
        1 * restTemplate.getForEntity("https://swapi.dev/api/starships/{id}/", Starship.class, 1)
                >> ResponseEntity.status(HttpStatus.OK).body(starship1)
        1 * restTemplate.getForEntity("https://swapi.dev/api/starships/{id}/", Starship.class, 2)
                >> ResponseEntity.status(HttpStatus.OK).body(starship2)
        resource instanceof Person
        resource.getName() == "Jack"
        resource.getHomeworld() instanceof Planet
        resource.getHomeworld().getName() == "Homeworld"
        resource.getStarships() instanceof Starship[]
        resource.getStarships().length == 2
        resource.getStarships()[0].getName() == "s1"
        resource.getStarships()[1].getName() == "s2"
    }

    def "when swapi gets an existing person which doesn't have homeworld or spaceships these fields should be null"() {
        given:
        Person person = new Person()
        person.setName("Jack")
        person.setHomeworldUrl("url.com/5/")
        person.setStarshipsUrls(new String[] {})

        when: "We request an existing resource"
        def resource = this.swapi.getPerson(1)

        then:
        1 * restTemplate.getForEntity("https://swapi.dev/api/people/{id}/", Person.class, 1)
                >> ResponseEntity.status(HttpStatus.OK).body(person)
        1 * restTemplate.getForEntity("https://swapi.dev/api/planets/{id}/", Planet.class, 5)
                >> ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        resource instanceof Person
        resource.getName() == "Jack"
        resource.getHomeworld() == null
        resource.getStarships() == null
    }


}
