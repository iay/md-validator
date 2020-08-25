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

package uk.org.iay.md.validator.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Element;

import net.shibboleth.metadata.pipeline.Pipeline;

/**
 * Represents a collection of validators.
 */
public class ValidatorCollection {

    /**
     * An entry representing a single available validator.
     */
    public static class Entry {

        /** Identifier for the validator. */
        private final String id;

        /** Description for the validator. */
        private final String description;

        /** Pipeline for the validator. */
        private final Pipeline<Element> pipeline;

        /**
         * Constructor.
         *
         * @param ctx {@link ApplicationContext} containing the validator.
         */
        public Entry(final ApplicationContext ctx) {
            id = ctx.getBean("id", String.class);
            description = ctx.getBean("description", String.class);

            @SuppressWarnings("unchecked")
            final Pipeline<Element> pipe = ctx.getBean("pipeline", Pipeline.class);
            pipeline = pipe;
        }

        /**
         * Return the validator's identifier.
         *
         * @return the validator's identifier.
         */
        public String getId() {
            return id;
        }

        /**
         * Return the validator's description.
         *
         * @return the validator's description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the validator's pipeline.
         *
         * @return the validator's pipeline
         */
        public Pipeline<Element> getPipeline() {
            return pipeline;
        }
    }

    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ValidatorCollection.class);

    /** Parent context to set on all the ones we load. */
    private final ApplicationContext parentContext;

    /** All entries as a list. */
    private final List<Entry> entries = new ArrayList<>();

    /** All entries indexed by identifier. */
    private final Map<String, Entry> byId = new HashMap<>();

    /**
     * Constructor.
     *
     * @param ctx parent {@link ApplicationContext} for all validators
     */
    public ValidatorCollection(final ApplicationContext ctx) {
        parentContext = ctx;
    }

    /**
     * Return a {@link List} of all entries.
     *
     * @return {@link List} of all entries
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Get the {@link Entry} with the given identifier, if any.
     *
     * @param id the identifier for the required {@link Entry}
     * @return the {@link Entry}, or <code>null</code>
     */
    public Entry getEntry(final String id) {
        return byId.get(id);
    }

    /**
     * Build an {@link ApplicationContext} from the indicated configuration,
     * and add it to the collection.
     *
     * @param configLocation classpath location for the {@link ApplicationContext}'s
     *  configuration
     */
    public void add(final String configLocation) {
        LOG.info("loading context {}", configLocation);
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
        ctx.setConfigLocation(configLocation);
        ctx.setParent(parentContext);
        ctx.setDisplayName(configLocation);
        ctx.refresh();
        LOG.info("context {} has {} bean definitions", configLocation, ctx.getBeanDefinitionCount());
        final Entry entry = new Entry(ctx);
        entries.add(entry);
        byId.put(entry.getId(), entry);
    }

}
