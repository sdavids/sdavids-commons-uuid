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

import static io.sdavids.commons.uuid.Uuids.fromShortenedRepresentationString;
import static io.sdavids.commons.uuid.Uuids.fromStandardRepresentationString;
import static io.sdavids.commons.uuid.Uuids.toShortenedRepresentationString;
import static io.sdavids.commons.uuid.Uuids.toStandardRepresentationString;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.LITERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("ClassCanBeStatic")
class UuidsTest {

  @Nested
  class FromStandardRepresentationString {

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
    void withFixed(String input, UUID expected, Class<? extends Exception> exception) {

      if (exception == null) {
        assertThat(fromStandardRepresentationString(input)).isEqualTo(expected);
      } else {
        assertThrows(
            exception,
            () -> assertThat(fromStandardRepresentationString(input)).isEqualTo(expected),
            "Invalid UUID string: " + input);
      }
    }

    @Test
    void withRandom() {
      for (int i = RUNS; i >= 0; i--) {
        UUID uuid = randomUUID();
        String str = uuid.toString();
        assertThat(fromStandardRepresentationString(str))
            .as("fromStandardRepresentationString(\"%s\")", str)
            .isEqualTo(uuid);
      }
    }
  }

  @Nested
  class FromShortenedRepresentationString {

    @ParameterizedTest
    @CsvSource({
      ",,java.lang.NullPointerException",
      "'',,java.lang.IllegalArgumentException",
      "0123456789012345678901234567890,,java.lang.IllegalArgumentException",
      "012345678901234567890123456789012,,java.lang.IllegalArgumentException",
      "abcdefABCDEF0123456789012345678h,, java.lang.IllegalArgumentException",
      "abcdefABCDEF0123456789012345678-,,java.lang.IllegalArgumentException",
      "01234567890123456789012345678901,01234567-8901-2345-6789-012345678901,",
      "abcdefabcdefabcdefabcdefabcdefab,abcdefab-cdef-abcd-efab-cdefabcdefab,",
      "ABCDEFABCDEFABCDEFABCDEFABCDEFAB,ABCDEFAB-CDEF-ABCD-EFAB-CDEFABCDEFAB,",
      "abcdefABCDEF01234567890123456789,abcdefAB-CDEF-0123-4567-890123456789,",
      "85a8b17f8ca54061aeb62f8a1a3bb60b,85a8b17f-8ca5-4061-aeb6-2f8a1a3bb60b,"
    })
    void withFixed(String input, UUID expected, Class<? extends Exception> exception) {

      if (exception == null) {
        assertThat(fromShortenedRepresentationString(input)).isEqualTo(expected);
      } else {
        assertThrows(
            exception,
            () -> assertThat(fromShortenedRepresentationString(input)).isEqualTo(expected),
            "Invalid UUID string: " + input);
      }
    }

    @Test
    void withRandom() {
      for (int i = RUNS; i >= 0; i--) {
        UUID uuid = randomUUID();
        String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
        assertThat(fromShortenedRepresentationString(str))
            .as("fromShortenedRepresentationString(\"%s\")", str)
            .isEqualTo(uuid);
      }
    }
  }

  @Nested
  class ToStandardRepresentationString {

    @Test
    void withFixed() {
      UUID uuid = KNOWN_UUID;
      String str = uuid.toString();
      assertThat(toStandardRepresentationString(uuid))
          .as("toStandardRepresentationString(%s)", uuid)
          .isEqualTo(str);
    }

    @Test
    void withRandom() {
      for (int i = RUNS; i >= 0; i--) {
        UUID uuid = randomUUID();
        String str = uuid.toString();
        assertThat(toStandardRepresentationString(uuid))
            .as("toStandardRepresentationString(%s)", uuid)
            .isEqualTo(str);
      }
    }
  }

  @Nested
  class ToShortenedRepresentationString {

    @Test
    void withFixed() {
      UUID uuid = KNOWN_UUID;
      String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
      assertThat(toShortenedRepresentationString(uuid))
          .as("toShortenedRepresentationString(%s)", uuid)
          .isEqualTo(str);
    }

    @Test
    void withRandom() {
      for (int i = RUNS; i >= 0; i--) {
        UUID uuid = randomUUID();
        String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
        assertThat(toShortenedRepresentationString(uuid))
            .as("toShortenedRepresentationString(%s)", uuid)
            .isEqualTo(str);
      }
    }
  }

  private static final int RUNS = 1000;

  private static final Pattern DASH_PATTERN = Pattern.compile("-", LITERAL);
  private static final UUID KNOWN_UUID = UUID.fromString("001c01d0-0c90-01A0-05e0-053a2fcb09b0");
}
