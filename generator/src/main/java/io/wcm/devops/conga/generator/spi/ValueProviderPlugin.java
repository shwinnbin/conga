/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.generator.spi;

import io.wcm.devops.conga.generator.spi.context.ValueProviderContext;

/**
 * Provides configuration values from external sources.
 */
public interface ValueProviderPlugin extends Plugin {

  /**
   * Gets parameter value from value provider.
   * @param variableName Variable name
   * @param context Context
   * @return Parameter value or null if none found
   */
  Object resolve(String variableName, ValueProviderContext context);

}
