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
package org.ocpsoft.rewrite.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.el.El;
import org.ocpsoft.rewrite.event.InboundRewrite;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.mock.MockEvaluationContext;
import org.ocpsoft.rewrite.param.Constraint;
import org.ocpsoft.rewrite.param.Converter;
import org.ocpsoft.rewrite.param.ParameterConfigurator;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.param.Validator;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.test.MockInboundRewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ConfigurationBuilderTest
{
   private boolean performed = false;
   private boolean performedOtherwise = false;
   private Operation operation;
   private Operation otherwise;
   private InboundRewrite rewrite;
   private EvaluationContext context;

   @Before
   public void before()
   {
      rewrite = new MockInboundRewrite();
      context = new MockEvaluationContext();
      operation = new Operation() {
         @Override
         public void perform(final Rewrite event, final EvaluationContext context)
         {
            performed = true;
         }
      };
      otherwise = new Operation() {
         @Override
         public void perform(final Rewrite event, final EvaluationContext context)
         {
            performedOtherwise = true;
         }
      };
   }

   private void execute(Configuration config)
   {
      for (Rule rule : config.getRules()) {
         if (rule.evaluate(rewrite, context))
            rule.perform(rewrite, context);
      }
   }

   @Test
   public void testBuildConfigurationPerformOnly()
   {
      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);

      Configuration config = ConfigurationBuilder.begin().addRule()
               .when(And.all(Direction.isInbound(), new True()))
               .perform(operation);

      execute(config);

      Assert.assertTrue(performed);
      Assert.assertFalse(performedOtherwise);
   }

   @Test
   public void testBuildConfigurationPerformOnlyNegative()
   {
      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);

      Configuration config = ConfigurationBuilder.begin().addRule()
               .when(And.all(new False()))
               .perform(operation);

      execute(config);

      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);
   }

   @Test
   public void testBuildConfigurationOtherwiseOnly()
   {
      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);

      Configuration config = ConfigurationBuilder.begin().addRule()
               .when(new False())
               .otherwise(otherwise);

      execute(config);

      Assert.assertFalse(performed);
      Assert.assertTrue(performedOtherwise);
   }

   @Test
   public void testBuildConfigurationOtherwiseNegative()
   {
      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);

      Configuration config = ConfigurationBuilder.begin().addRule()
               .when(new True())
               .otherwise(otherwise);

      execute(config);

      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);
   }

   @Test
   public void testBuildConfigurationPerformAndOtherwise() throws Exception
   {
      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);

      Configuration config = ConfigurationBuilder.begin()
               .addRule()
               .when(new True())
               .perform(operation)
               .otherwise(otherwise);

      execute(config);

      Assert.assertTrue(performed);
      Assert.assertFalse(performedOtherwise);
   }

   @Test
   public void testBuildConfigurationPerformAndOtherwiseNegative() throws Exception
   {
      Assert.assertFalse(performed);
      Assert.assertFalse(performedOtherwise);

      Configuration config = ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .perform(operation)
               .otherwise(otherwise);

      execute(config);

      Assert.assertFalse(performed);
      Assert.assertTrue(performedOtherwise);
   }

   @Test
   public void testSubsetAPI() throws Exception
   {
      ConfigurationBuilder.begin()

               .addRule()
               .when(new False())
               .otherwise(Subset.evaluate(ConfigurationBuilder.begin()
                        .addRule()
                        .when(new True())
                        .perform(operation)));
   }

   @Test
   public void testWhereAPI() throws Exception
   {
      ParameterConfigurator configurator = new ParameterConfigurator() {};
      Validator<String> validator = new Validator<String>() {
         @Override
         public boolean isValid(Rewrite event, EvaluationContext context, String value)
         {
            return false;
         }
      };
      Transposition<String> transposition = new Transposition<String>() {

         @Override
         public String transpose(Rewrite event, EvaluationContext context, String value)
         {
            return null;
         }
      };
      Converter<String> converter = new Converter<String>() {
         @Override
         public String convert(Rewrite event, EvaluationContext context, Object value)
         {
            return null;
         }
      };
      Constraint<String> constraint = new Constraint<String>() {
         @Override
         public boolean isSatisfiedBy(Rewrite event, EvaluationContext context, String value)
         {
            return false;
         }
      };

      ConfigurationBuilder.begin()

               .addRule()
               .when(Path.matches("/{p}/{s}"))
               .perform(operation)
               .otherwise(operation)
               .where("p").bindsTo(El.property("whee.glee")).matches("blah")
               .constrainedBy(null).convertedBy(null).transposedBy(null).validatedBy(null)
               .where("s").matches("oh").bindsTo(El.property("ee.flee"))
               .withPriority(0)
               .withId("id")

               .addRule()
               .when(Path.matches("/{p}/{s}"))
               .perform(operation)
               .where("p").matches("blah").bindsTo(El.property("whee.glee"))
               .where("s").matches("oh").bindsTo(El.property("ee.flee"))
               .constrainedBy(constraint).convertedBy(converter).transposedBy(transposition).validatedBy(validator)
               .configuredBy(configurator)
               .withId("id")
               .withPriority(0)

               .addRule()
               .when(Path.matches("/{p}/{s}"))
               .otherwise(operation)
               .where("p")
               .configuredBy(constraint).configuredBy(converter).configuredBy(transposition).configuredBy(validator);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testConfigureUndefinedParameter() throws Exception
   {
      ConfigurationBuilder.begin()
               .addRule()
               .when(new True())
               .perform(operation)
               .otherwise(operation)
               .where("p");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testConfigureUndefinedParameter2() throws Exception
   {
      ConfigurationBuilder.begin()
               .addRule()
               .when(new True())
               .otherwise(operation)
               .where("p");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testConfigureUndefinedParameter3() throws Exception
   {
      ConfigurationBuilder.begin()
               .addRule()
               .when(new True())
               .perform(operation)
               .where("p");
   }

   @Test
   public void testFluentAPI() throws Exception
   {
      ConfigurationBuilder.begin()
               .addRule()
               .perform(operation).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .perform(operation)
               .withPriority(0).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .perform(operation)
               .withId("id")
               .withPriority(0).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .perform(operation)
               .otherwise(otherwise)
               .withId("id")
               .withPriority(0).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .perform(operation)
               .otherwise(otherwise)
               .withId("id")
               .withPriority(0).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .perform(operation)
               .otherwise(otherwise)
               .withPriority(0)
               .withId("id").addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .otherwise(otherwise)
               .withPriority(0)
               .withId("id").addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .otherwise(otherwise)
               .withId("id")
               .withPriority(0).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .otherwise(otherwise)
               .withPriority(0).addRule();

      ConfigurationBuilder.begin()
               .addRule()
               .when(new False())
               .otherwise(otherwise).addRule();
   }

}
