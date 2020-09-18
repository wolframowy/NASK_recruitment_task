import com.nask.task.TaskApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

@ContextConfiguration(classes= TaskApplication.class)
@WebMvcTest
@AutoConfigureMockMvc
class CharactersControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

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

    def "when get is performed with an path variable 'id' then the response has status 200 and content is 'Requested id'"() {
        expect: "Status is 200 and the response is 'Requested page 7'"
        this.mvc.perform(MockMvcRequestBuilders.get("/characters/7"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response
                .contentAsString == "Requested 7"
    }
}