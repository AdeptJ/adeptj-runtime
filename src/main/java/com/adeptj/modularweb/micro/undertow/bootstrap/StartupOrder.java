package com.adeptj.modularweb.micro.undertow.bootstrap;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated type's {@link StartupHandler#onStartup(javax.servlet.ServletContext)} must be call in the 
 * StartupOrder#order() specified.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface StartupOrder {

	public int value() default 0;
}
