/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/
package com.adeptj.runtime.jaxrs;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider;
import org.jboss.resteasy.plugins.validation.GeneralValidatorImpl;
import org.jboss.resteasy.plugins.validation.i18n.Messages;
import org.jboss.resteasy.spi.validation.GeneralValidator;

import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * GeneralValidatorContextResolver.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@Provider
public class GeneralValidatorContextResolver implements ContextResolver<GeneralValidator> {

	private volatile ValidatorFactory validatorFactory;

	private static final Set<ExecutableType> ALL_VALIDATED_EXECUTABLE_TYPES = Collections.unmodifiableSet(
			EnumSet.complementOf(EnumSet.of(ExecutableType.ALL, ExecutableType.NONE, ExecutableType.IMPLICIT)));

	@Override
	public GeneralValidator getContext(Class<?> type) {
		try {
			if (this.validatorFactory == null) {
				this.validatorFactory = ValidatorFactoryInitializer.INSTANCE.getValidatorFactory();
			}
			return new GeneralValidatorImpl(this.validatorFactory, true, ALL_VALIDATED_EXECUTABLE_TYPES);
		} catch (Exception ex) {
			throw new ValidationException(Messages.MESSAGES.unableToLoadValidationSupport(), ex);
		}
	}

	private enum ValidatorFactoryInitializer {

		INSTANCE;

		private ValidatorFactory getValidatorFactory() {
			HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
			config.parameterNameProvider(new ReflectionParameterNameProvider());
			return config.buildValidatorFactory();
		}
	}
}
