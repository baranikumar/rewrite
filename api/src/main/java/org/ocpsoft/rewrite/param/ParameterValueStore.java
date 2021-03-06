/*
 * Copyright 2013 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
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
package org.ocpsoft.rewrite.param;

import org.ocpsoft.rewrite.bind.Binding;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * Used to submit {@link Parameter} values in {@link String} form. These values will subsequently be passed through
 * {@link Constraint}, {@link Transposition} and {@link Binding} processing.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface ParameterValueStore
{
   /**
    * Submit the given {@link Parameter} and value. Return false if the value does not match configured
    * {@link Constraint} instances, or does not match an already submitted value.
    */
   boolean submit(Rewrite event, EvaluationContext context, Parameter<?> param, String value);

   /**
    * Retrieve the value for the given {@link Parameter}
    */
   String retrieve(Parameter<?> parameter);
}
