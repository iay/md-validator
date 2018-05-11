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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    /** Current {@link HttpServletRequest}. */
    private final HttpServletRequest request;

    /**
     * Constructor.
     *
     * @param req current {@link HttpServletRequest}.
     * @param valc {@link ValidatorCollection}
     */
    @Autowired
    public ValidatorsApiController(final HttpServletRequest req, final ValidatorCollection valc) {
        request = req;
        validatorCollection = valc;
    }

    /**
     * Make a {@link Validator} from an identifier and description.
     *
     * @param id identifier for the validator
     * @param description description of the validator
     * @return a {@link Validator}
     */
    private Validator makeValidator(final String id, final String description) {
        final Validator v = new Validator();
        v.setValidatorId(id);
        v.setDescription(description);
        return v;
    }

    @Override
    public ResponseEntity<List<Validator>> getValidators() {
        final String accept = request.getHeader("Accept");
        LOG.info("accept {}", accept);
        if (accept != null && accept.contains("application/json")) {
            final List<Validator> validators = new ArrayList<>();
            for (final ValidatorCollection.Entry entry : validatorCollection.getEntries()) {
                validators.add(makeValidator(entry.getId(), entry.getDescription()));
            }
            return new ResponseEntity<List<Validator>>(validators, HttpStatus.OK);
        }

        return new ResponseEntity<List<Validator>>(HttpStatus.NOT_IMPLEMENTED);
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
                final String metadata) {
        final String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            final List<Status> statuses = new ArrayList<>();
            statuses.add(makeStatus(StatusEnum.ERROR, "component", "message"));
            statuses.add(makeStatus(StatusEnum.WARNING, "component/sub", "another message"));
            return new ResponseEntity<List<Status>>(statuses, HttpStatus.NOT_IMPLEMENTED);
        }

        return new ResponseEntity<List<Status>>(HttpStatus.NOT_IMPLEMENTED);
    }
}
