package com.ihidea.core.support.mongo;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({MongoPlusAutoConfiguration.class})
public @interface EnableMongoPlus {

}
