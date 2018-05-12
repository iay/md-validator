/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.iay.incommon.validator.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;
import uk.org.iay.incommon.validator.context.ValidatorCollection;
import uk.org.iay.incommon.validator.models.Status;
import uk.org.iay.incommon.validator.models.Status.StatusEnum;
import uk.org.iay.incommon.validator.models.Validator;

/**
 * Controller for the Validators API.
 */
@Controller
public class ValidatorsApiController implements ValidatorsApi {

    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ValidatorsApiController.class);

    /** Collection of all validators known to us. */
    private final ValidatorCollection validatorCollection;

    /**
     * Constructor.
     *
     * @param valc {@link ValidatorCollection}
     */
    @Autowired
    public ValidatorsApiController(final ValidatorCollection valc) {
        validatorCollection = valc;
    }

    @Override
    public ResponseEntity<List<Validator>> getValidators() {
        final List<Validator> validators = new ArrayList<>();
        for (final ValidatorCollection.Entry entry : validatorCollection.getEntries()) {
            final Validator v = new Validator();
            v.setValidatorId(entry.getId());
            v.setDescription(entry.getDescription());
            validators.add(v);
        }
        return new ResponseEntity<List<Validator>>(validators, HttpStatus.OK);
    }

    /**
     * Make a {@link Status} from its components.
     *
     * @param status a {@link StatusEnum} indicating the kind of status
     * @param componentId identifier for the generating component
     * @param message message associated with the status
     * @return a {@link Status}
     */
    private Status makeStatus(final StatusEnum status, final String componentId, final String message) {
        final Status s = new Status();
        s.setStatus(status);
        return s;
    }

    @Override
    public ResponseEntity<List<Status>> validate(
            @ApiParam(value = "An identifier for the validation to be performed.", required = true)
                @PathVariable("validator_id")
                final String validatorId,
            @ApiParam(value = "The metadata to be validated.", required = true) @Valid @RequestBody
                final String metadata) throws ApiException {

        // Fetch the required validator.
        final ValidatorCollection.Entry entry = validatorCollection.getEntry(validatorId);
        if (entry == null) {
            throw new NotFoundException("unknown validator identifier '" + validatorId + "'");
        }

        final List<Status> statuses = new ArrayList<>();
        statuses.add(makeStatus(StatusEnum.ERROR, "component", "message"));
        statuses.add(makeStatus(StatusEnum.WARNING, "component/sub", "another message"));
        return new ResponseEntity<List<Status>>(statuses, HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Handle an {@link ApiException} by converting it to a {@link Map}, which will
     * in turn be converted to a JSON object.
     *
     * @param req the {@link HttpServletRequest} being handled
     * @param ex the {@link ApiException} being handled
     * @return a {@link ResponseEntity} equivalent to the {@link ApiException}
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(final HttpServletRequest req, final ApiException ex) {
        final Map<String, Object> m = ex.toMap();
        m.put("path", req.getRequestURI());
        return new ResponseEntity<>(m, ex.getStatus());
    }
}
