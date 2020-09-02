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


package uk.org.iay.md.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.org.iay.md.validator.context.ValidatorCollection;

/**
 * Validator configuration.
 */
@Configuration
@ImportResource("classpath:root-beans.xml")
@ComponentScan(basePackages = "io.swagger.configuration")
public class ValidatorConfiguration {

    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ValidatorConfiguration.class);

    /** Property value telling us which validator configurations to load. */
    @Value("${validator.configurations}")
    private String configurations;

    /** Property value telling us where the common configuration lives. */
    @Value("${validator.common}")
    private String commonConfiguration;

    /**
     * Build the validator configuration collection.
     *
     * @param webContext the web application context
     * @return the {@link ValidatorCollection} bean
     */
    @Bean
    public ValidatorCollection validatorCollection(final ApplicationContext webContext) {
        // Build the common configuration.
        LOG.info("loading common configuration from {}", commonConfiguration);
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
        ctx.setConfigLocation(commonConfiguration);
        ctx.setParent(webContext);
        ctx.setDisplayName(commonConfiguration);
        ctx.refresh();
        LOG.info("common configuration has {} bean definitions", ctx.getBeanDefinitionCount());

        // Build the individual validators, with the common configuration as a parent.
        final ValidatorCollection c = new ValidatorCollection(ctx);
        LOG.info("loading validator configurations: '{}'", configurations);
        for (final String configLocation : configurations.split("\\s+")) {
            c.add(configLocation);
        }
        return c;
    }

}
