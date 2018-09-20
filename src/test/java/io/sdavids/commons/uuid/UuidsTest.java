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

import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class UuidsTest {

  private static final int RUNS = 1000;

  private static final Pattern DASH_PATTERN = Pattern.compile("-", LITERAL);
  private static final UUID KNOWN_UUID = UUID.fromString("001c01d0-0c90-01A0-05e0-053a2fcb09b0");

  @Test
  void fromStandardRepresentationString_() {
    UUID uuid = KNOWN_UUID;
    String str = uuid.toString();
    assertThat(fromStandardRepresentationString(str))
        .as("fromStandardRepresentationString(\"%s\")", str)
        .isEqualTo(uuid);
  }

  @Test
  void fromShortenedRepresentationString_() {
    UUID uuid = KNOWN_UUID;
    String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
    assertThat(fromShortenedRepresentationString(str))
        .as("fromShortenedRepresentationString(\"%s\")", str)
        .isEqualTo(uuid);
  }

  @Test
  void fromStandardRepresentationString_random() {
    for (int i = RUNS; i >= 0; i--) {
      UUID uuid = randomUUID();
      String str = uuid.toString();
      assertThat(fromStandardRepresentationString(str))
          .as("fromStandardRepresentationString(\"%s\")", str)
          .isEqualTo(uuid);
    }
  }

  @Test
  void fromShortenedRepresentationString_random() {
    for (int i = RUNS; i >= 0; i--) {
      UUID uuid = randomUUID();
      String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
      assertThat(fromShortenedRepresentationString(str))
          .as("fromShortenedRepresentationString(\"%s\")", str)
          .isEqualTo(uuid);
    }
  }

  @Test
  void toStandardRepresentationString_() {
    UUID uuid = KNOWN_UUID;
    String str = uuid.toString();
    assertThat(toStandardRepresentationString(uuid))
        .as("toStandardRepresentationString(%s)", uuid)
        .isEqualTo(str);
  }

  @Test
  void toShortenedRepresentationString_() {
    UUID uuid = KNOWN_UUID;
    String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
    assertThat(toShortenedRepresentationString(uuid))
        .as("toShortenedRepresentationString(%s)", uuid)
        .isEqualTo(str);
  }

  @Test
  void toStandardRepresentationString_random() {
    for (int i = RUNS; i >= 0; i--) {
      UUID uuid = randomUUID();
      String str = uuid.toString();
      assertThat(toStandardRepresentationString(uuid))
          .as("toStandardRepresentationString(%s)", uuid)
          .isEqualTo(str);
    }
  }

  @Test
  void toShortenedRepresentationString_random() {
    for (int i = RUNS; i >= 0; i--) {
      UUID uuid = randomUUID();
      String str = DASH_PATTERN.matcher(uuid.toString()).replaceAll("");
      assertThat(toShortenedRepresentationString(uuid))
          .as("toShortenedRepresentationString(%s)", uuid)
          .isEqualTo(str);
    }
  }
}
