/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.devops.conga.generator.plugins.handlebars.helper;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;

import java.io.IOException;
import java.util.regex.Pattern;

import com.github.jknack.handlebars.Options;

/**
 * Handlebars helper that allows to quotes a string for usage in a regular expression.
 */
public class RegexQuoteHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "regexQuote";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public CharSequence apply(Object context, Options options) throws IOException {
    if (options.isFalsy(context)) {
      Object param = options.param(0, null);
      return param == null ? null : param.toString();
    }
    return Pattern.quote(context.toString());
  }

}