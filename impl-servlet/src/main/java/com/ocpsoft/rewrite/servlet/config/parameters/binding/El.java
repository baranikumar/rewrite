/*
 * Copyright 2011 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocpsoft.rewrite.servlet.config.parameters.binding;

import org.jboss.logging.Logger;

import com.ocpsoft.rewrite.EvaluationContext;
import com.ocpsoft.rewrite.config.Operation;
import com.ocpsoft.rewrite.services.ServiceLoader;
import com.ocpsoft.rewrite.servlet.config.HttpOperation;
import com.ocpsoft.rewrite.servlet.config.parameters.Converter;
import com.ocpsoft.rewrite.servlet.config.parameters.ParameterBindingBuilder;
import com.ocpsoft.rewrite.servlet.config.parameters.Validator;
import com.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;
import com.ocpsoft.rewrite.servlet.spi.ElSupportProvider;

/**
 * TODO arquillian test
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class El extends ParameterBindingBuilder
{
   private final String property;
   private final Logger log = Logger.getLogger(El.class);

   public El(final String property)
   {
      this.property = property;
   }

   public static El property(final String property)
   {
      return new El(property);
   }

   public static El property(final String property, final Class<? extends Converter<?>> type)
   {
      El el = new El(property);
      el.convertedBy(type);
      return el;
   }

   public static El property(final String property, final Class<? extends Converter<?>> converterType,
            final Class<? extends Validator<?>> validatorType)
   {
      El el = new El(property);
      el.convertedBy(converterType);
      el.validatedBy(validatorType);
      return el;
   }

   private class ElBindingOperation extends HttpOperation
   {
      private final String property;
      private final Object value;

      public ElBindingOperation(final String property, final Object value)
      {
         this.property = property;
         this.value = value;
      }

      @Override
      @SuppressWarnings("unchecked")
      public void performHttp(final HttpServletRewrite event, final EvaluationContext context)
      {
         ServiceLoader<ElSupportProvider> providers = ServiceLoader.load(ElSupportProvider.class);

         for (ElSupportProvider provider : providers) {
            try
            {
               provider.injectValue(property, value);
               break;
            }
            catch (Exception e) {
               log.debug("El provider [" + provider.getClass().getName() + "] could not inject property #{" + property
                        + "} with value [" + value + "]");
            }
         }
      }
   }

   @Override
   public Operation getOperation(final HttpServletRewrite event, final EvaluationContext context, final Object value)
   {
      return new ElBindingOperation(property, value);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Object extractBoundValue(final HttpServletRewrite event, final EvaluationContext context)
   {
      ServiceLoader<ElSupportProvider> providers = ServiceLoader.load(ElSupportProvider.class);

      Object value = null;
      for (ElSupportProvider provider : providers) {

         try
         {
            value = provider.extractValue(property);
            break;
         }
         catch (Exception e) {
            log.debug("El provider [" + provider.getClass().getName() + "] could not extract value from property #{"
                     + property + "}");
         }

         if (value != null)
         {
            break;
         }
      }

      return value;
   }
}
