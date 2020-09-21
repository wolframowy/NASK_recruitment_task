import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.nask.task.TaskApplication
import com.nask.task.models.Person
import com.nask.task.services.Swapi
import org.spockframework.spring.SpringBean
import org.spockframework.spring.SpringSpy
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

// TODO: Fix test cases!!!
@ContextConfiguration(classes= TaskApplication.class)
@WebMvcTest
@AutoConfigureMockMvc
class CharactersControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

    @SpringBean
    private Swapi swapi = Mock(Swapi)

    def "when get is performed with no argument then the response has status 200 and content is 'Requested page 1'"() {
        expect: "Status is 200 and the response is 'Requested page 1'"
        this.mvc.perform(MockMvcRequestBuilders.get("/characters"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response
                .contentAsString == "Requested page 1"
    }

    def "when get is performed with a param 'n' then the response has status 200 and content is 'Requested page n'"() {
        expect: "Status is 200 and the response is 'Requested page 1'"
        this.mvc.perform(MockMvcRequestBuilders.get("/characters?page=3"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response
                .contentAsString == "Requested page 3"
    }

    def "when non-existing character is requested return 404 with error message"() {
        given:
        swapi.getPerson(8) >> null

        expect:
        this.mvc.perform(MockMvcRequestBuilders.get("/characters/8"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn()
                .response
                .errorMessage == "Requested person with id 8 not found"
    }

    def "when character is requested it should json string with appropriate fields filled"() {
        given:
        Person person = new Person()
        person.setName("Jack")
        person.setGender("male")
        swapi.getPerson(8) >> person
        String providedObject = "{\"id\":null,\"name\":\"Jack\",\"height\":null,\"mass\":null,\"gender\":\"male\",\"homeworld\":null,\"starships\":null,\"hair_color\":null,\"skin_color\":null,\"eye_color\":null,\"birth_year\":null}"

        expect:
        def response = this.mvc.perform(MockMvcRequestBuilders.get("/characters/8"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response

        response.contentType == "application/json"
        response.contentAsString == providedObject
    }
}