
package uk.org.iay.incommon.validator.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;
import uk.org.iay.incommon.validator.models.Status;
import uk.org.iay.incommon.validator.models.Validator;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
        date = "2018-05-11T08:14:22.184+01:00")

@Controller
public class ValidatorsApiController implements ValidatorsApi {

    private static final Logger log = LoggerFactory.getLogger(ValidatorsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public ValidatorsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<List<Validator>> getValidators() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<Validator>>(objectMapper.readValue(
                        "[ {  \"validator_id\" : \"eduGAIN\",  \"description\" : \"validation ruleset for entities from eduGAIN\"}, {  \"validator_id\" : \"eduGAIN\",  \"description\" : \"validation ruleset for entities from eduGAIN\"} ]",
                        List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<Validator>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<Validator>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<Status>> validate(
            @ApiParam(value = "An identifier for the validation to be performed. ",
                    required = true) @PathVariable("validator_id") String validatorId,
            @ApiParam(value = "The metadata to be validated.", required = true) @Valid @RequestBody String metadata) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<Status>>(objectMapper.readValue(
                        "[ {  \"status\" : \"error\",  \"componentId\" : \"checkSchemas\",  \"message\" : \"the entityID doesn't have the correct format\"}, {  \"status\" : \"error\",  \"componentId\" : \"checkSchemas\",  \"message\" : \"the entityID doesn't have the correct format\"} ]",
                        List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<Status>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<Status>>(HttpStatus.NOT_IMPLEMENTED);
    }

}
