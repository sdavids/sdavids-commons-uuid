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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class UuidsFromStandardRepresentationStringTest {

  @ParameterizedTest
  @CsvSource({
    ",,java.lang.NullPointerException",
    "'',,java.lang.IllegalArgumentException",
    "01234567890123456789012345678901234,,java.lang.IllegalArgumentException",
    "0123456789012345678901234567890123456,,java.lang.IllegalArgumentException",
    "012345678901234567890123456789012345,,java.lang.IllegalArgumentException",
    "012345678901234567890123456789012---,,java.lang.IllegalArgumentException",
    "0123456789012345678901234567890-----,,java.lang.IllegalArgumentException",
    "0123456789012345678901234567890----,,java.lang.IllegalArgumentException",
    "012345678901234567890123456789012----,,java.lang.IllegalArgumentException",
    "abcdefABCDEF0123456789012345678h----,,java.lang.IllegalArgumentException",
    "01234567890123456789012345678901----,,java.lang.IllegalArgumentException",
    "abcdefabcdefabcdefabcdefabcdefab----,,java.lang.IllegalArgumentException",
    "ABCDEFABCDEFABCDEFABCDEFABCDEFAB----,,java.lang.IllegalArgumentException",
    "abcdefABCDEF01234567890123456789----,,java.lang.IllegalArgumentException",
    "85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60h,,java.lang.IllegalArgumentException",
    "f424e90b17f0f-4e71-99ed-e8c8e7be58e2,,java.lang.IllegalArgumentException",
    "f424e90b-7f0f24e71-99ed-e8c8e7be58e2,,java.lang.IllegalArgumentException",
    "f424e90b-7f0f-4e71399ed-e8c8e7be58e2,,java.lang.IllegalArgumentException",
    "f424e90b-7f0f-4e71-99ed4e8c8e7be58e2,,java.lang.IllegalArgumentException",
    "f424e90b-7f0f-4e71-99ed-e8c8-7be58e2,,java.lang.IllegalArgumentException",
    "85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60b,85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60b,"
  })
  void fromStandardRepresentationString(
      String input, UUID expected, Class<? extends Exception> exception) {

    if (exception == null) {
      assertThat(Uuids.fromStandardRepresentationString(input)).isEqualTo(expected);
    } else {
      assertThrows(
          exception,
          () -> assertThat(Uuids.fromStandardRepresentationString(input)).isEqualTo(expected),
          "Invalid UUID string: " + input);
    }
  }
}
