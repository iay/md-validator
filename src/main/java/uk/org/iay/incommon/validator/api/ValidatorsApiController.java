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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.swagger.annotations.ApiParam;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.StatusMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.PipelineProcessingException;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
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

    /** Pool of XML parsers. */
    private final ParserPool parserPool;

    /**
     * Constructor.
     *
     * @param valc {@link ValidatorCollection}
     * @param pool {@link ParserPool}
     */
    @Autowired
    public ValidatorsApiController(final ValidatorCollection valc, final ParserPool pool) {
        validatorCollection = valc;
        parserPool = pool;
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
        return new ResponseEntity<>(validators, HttpStatus.OK);
    }

    /**
     * Convert a {@link StatusMetadata} from a pipeline result into
     * a {@link Status} for transmission to the client.
     *
     * @param stat the {@link StatusMetadata} to convert
     * @return a converted {@link Status}
     */
    private Status convertStatus(final StatusMetadata stat) {
        final Status s = new Status();
        s.setComponentId(stat.getComponentId());
        s.setMessage(stat.getStatusMessage());
        if (stat instanceof ErrorStatus) {
            s.setStatus(StatusEnum.ERROR);
        } else if (stat instanceof WarningStatus) {
            s.setStatus(StatusEnum.WARNING);
        } else if (stat instanceof InfoStatus) {
            s.setStatus(StatusEnum.INFO);
        } else {
            LOG.error("unknown StatusMetadata subtype {}", stat.getClass().getName());
            s.setStatus(StatusEnum.ERROR);
        }
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

        // Parse the body to create an Item<Element>.
        final Item<Element> item;
        try {
            final Document doc = parserPool.parse(new StringReader(metadata));
            item = new DOMElementItem(doc);
        } catch (final XMLParserException ex) {
            LOG.info("XLMParserException: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "XMLParserException: " + ex.getMessage(), ex.getCause());
        }

        // Perform sanity checks
        sanityCheckDocument(item);

        // Form the item collection.
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        // Run the validator.
        final Pipeline<Element> pipeline = entry.getPipeline();
        try {
            pipeline.execute(items);
        } catch (final PipelineProcessingException ex) {
            LOG.info("Pipeline failed: {}", ex.getMessage());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Pipeline failed: " + ex.getMessage(), ex.getCause());
        }

        // Build the response from any resulting statuses
        final List<StatusMetadata> sts = item.getItemMetadata().get(StatusMetadata.class);
        final List<Status> statuses = new ArrayList<>();
        for (final StatusMetadata st : sts) {
            statuses.add(convertStatus(st));
        }
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }

    /**
     * Perform basic sanity checks on a metadata document.
     *
     * This is intended to catch grossly malformed metadata before it is run through
     * a pipeline.
     *
     * @param item the {@link Item} to sanity check
     * @throws ApiException if the document isn't well formed
     */
    private void sanityCheckDocument(@Nonnull final Item<Element> item) throws ApiException {
        // Check namespace
        final Element docElement = item.unwrap();
        final String namespace = docElement.getNamespaceURI();
        if (namespace == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "document root does not have a namespace");
        }
        if (!SAMLMetadataSupport.MD_NS.equals(namespace)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "document root has wrong namespace " + namespace);
        }

        // Check element name
        if (!SAMLMetadataSupport.isEntityDescriptor(docElement)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "document element is not a SAML entity descriptor");
        }

        // Check that there is an entityID attribute
        if (docElement.getAttributeNodeNS(null, "entityID") == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "entity has no entityID");
        }
        final String entityID = docElement.getAttributeNS(null, "entityID");
        if (entityID.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "entity has empty entityID");
        }
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
