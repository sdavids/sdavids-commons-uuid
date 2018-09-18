/*
 * Copyright (c) 2017, Sebastian Davids
 *
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
 */
package io.sdavids.commons.uuid;

import static io.sdavids.commons.uuid.Uuids.fromStandardRepresentationString;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public final class UuidsFromStandardRepresentationStringTest {

  @Parameters(name = "{index}: fromStandardRepresentationString({0})={1}")
  public static Collection<Object[]> data() {
    return asList(
        new Object[][] {
          {null, null, NullPointerException.class},
          {"", null, IllegalArgumentException.class},
          {"01234567890123456789012345678901234", null, IllegalArgumentException.class},
          {"0123456789012345678901234567890123456", null, IllegalArgumentException.class},
          {"012345678901234567890123456789012345", null, IllegalArgumentException.class},
          {"012345678901234567890123456789012---", null, IllegalArgumentException.class},
          {"0123456789012345678901234567890-----", null, IllegalArgumentException.class},
          {"0123456789012345678901234567890----", null, IllegalArgumentException.class},
          {"012345678901234567890123456789012----", null, IllegalArgumentException.class},
          {"abcdefABCDEF0123456789012345678h----", null, IllegalArgumentException.class},
          {"01234567890123456789012345678901----", null, IllegalArgumentException.class},
          {"abcdefabcdefabcdefabcdefabcdefab----", null, IllegalArgumentException.class},
          {"ABCDEFABCDEFABCDEFABCDEFABCDEFAB----", null, IllegalArgumentException.class},
          {"abcdefABCDEF01234567890123456789----", null, IllegalArgumentException.class},
          {"85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60h", null, IllegalArgumentException.class},
          {"f424e90b17f0f-4e71-99ed-e8c8e7be58e2", null, IllegalArgumentException.class},
          {"f424e90b-7f0f24e71-99ed-e8c8e7be58e2", null, IllegalArgumentException.class},
          {"f424e90b-7f0f-4e71399ed-e8c8e7be58e2", null, IllegalArgumentException.class},
          {"f424e90b-7f0f-4e71-99ed4e8c8e7be58e2", null, IllegalArgumentException.class},
          {"f424e90b-7f0f-4e71-99ed-e8c8-7be58e2", null, IllegalArgumentException.class},
          {
            "85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60b",
            UUID.fromString("85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60b"),
            null
          },
        });
  }

  @Parameter public String input;

  @Parameter(1)
  public UUID expected;

  @Parameter(2)
  public Class<? extends Exception> exception;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    if (exception != null) {
      thrown.expect(exception);
      if (Objects.equals(exception, IllegalArgumentException.class)) {
        thrown.expectMessage("Invalid UUID string: " + input);
      }
    }
    assertThat(fromStandardRepresentationString(input)).isEqualTo(expected);
  }
}
