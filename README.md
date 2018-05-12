# `inc-validator`

A micro-service to perform SAML metadata validation.

Built using:

* OpenAPI 2 and Swagger
* Shibboleth Metadata Aggregator
* Spring Boot

## Spring Contexts

The root `ApplicationContext` in the application is the one provided by
Spring Boot. This is primarily configured using Java annotations, but
it would be possible to extend that with XML configuration if needed.

The classpath resource `common-beans.xml` is used to configure a
`ClassPathXmlApplicationContext` which takes the root context as its
parent. The `common-beans.xml` context should be used to provide any
beans which will be useful in all validators, so that they don't end up
with a lot of duplication.

Each validator lives in a `ClassPathXmlApplicationContext` of its own.
These are loaded from classpath resources named in the property
`validator.configurations`. The validator contexts are given the
`common-beans.xml` context as their parent.

Each validator context must have the following beans:

* A `String` bean called `id`, which becomes the unique identifier for the
  validator.

* A `String` bean called `description`, which provides a description for the
  validator for use at the `/validators` endpoint.

* A `Pipeline<Element>` bean called `pipeline`, which is the metadata aggregator
  pipeline to execute to perform validation.

* A unnamed `IdentifiableBeanPostProcessor` bean which takes care of copying
  Spring bean `id` attributes into the corresponding Shibboleth component `id`
  if the latter is not supplied. Note that this behaviour only applies for beans
  in the same context as the `IdentifiableBeanPostProcessor` bean and is not
  inherited from the `common-beans.xml` context.

## Copyright and License

The entire package is Copyright (C) 2018, Ian A. Young.

Licensed under the Apache License, Version 2.0.
