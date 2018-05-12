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

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

/**
 * Exception class for use within the API.
 */
public class ApiException extends Exception {

    /** Serial version UID. */
    private static final long serialVersionUID = -5046975083822313941L;

    /** HTTP Status to be reported by this exception. */
    private final HttpStatus status;

    /** When the exception was created. */
    private final DateTime when;

    /**
     * Constructor.
     *
     * @param stat HTTP status
     * @param msg message
     */
    public ApiException(final HttpStatus stat, final String msg) {
        super(msg);
        status = stat;
        when = DateTime.now();
    }

    /**
     * Returns the wrapped {@link HttpStatus} value.
     *
     * @return an {@link HttpStatus}
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Convert to a {@link Map} so that the data can be turned into
     * a JSON response.
     *
     * @return a new {@link Map} containing the exception information
     */
    public Map<String, Object> toMap() {
        final Map<String, Object> m = new HashMap<>();
        m.put("status", Integer.valueOf(getStatus().value()));
        m.put("error", getStatus().getReasonPhrase());
        m.put("message", getMessage());
        m.put("exception", getClass().getName());
        m.put("timestamp", when);
        return m;
    }
}
