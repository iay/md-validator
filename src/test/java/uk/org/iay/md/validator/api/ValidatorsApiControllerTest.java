
package uk.org.iay.md.validator.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import net.shibboleth.metadata.BaseTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ValidatorsApiControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    /** Helper class for resources. */
    private BaseTest test = new BaseTest(ValidatorsApiController.class) {  };

    private final static MediaType SAML_METADATA = new MediaType("application", "xml+samlmetadata");

    @Test
    public void testValidators() throws Exception {
        mockMvc.perform(get("/validators"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
            .andExpect(jsonPath("$[2]").isMap())
            .andExpect(jsonPath("$[2].validator_id", is("test")));
    }
    

    @Test
    public void testTestValidation() throws Exception {
        final var xml = test.readBytes("valid.xml");
        mockMvc.perform(post("/validators/test/validate")
                .content(xml)
                .header(HttpHeaders.CONTENT_TYPE, SAML_METADATA)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].componentId", is("fake_warn")));
    }
    
    @Test
    public void testSchemaFailure() throws Exception {
        final var xml = test.readBytes("schema.xml");
        mockMvc.perform(post("/validators/test/validate")
                .content(xml)
                .header(HttpHeaders.CONTENT_TYPE, SAML_METADATA)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].componentId", is("checkSchemas")))
            .andExpect(jsonPath("$[0].message", startsWith("cvc-complex-type.2.4.a")));
    }
    
    @Test
    public void testGitHub6() throws Exception {
        final var xml = test.readBytes("valid.xml");
        mockMvc.perform(post("/validators/github-6/validate")
                .content(xml)
                .header(HttpHeaders.CONTENT_TYPE, SAML_METADATA)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].componentId", is("fake_warn")))
            .andExpect(jsonPath("$[1].componentId", is("fake_info")));
    }
}
