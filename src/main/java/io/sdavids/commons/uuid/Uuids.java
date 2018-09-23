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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Miscellaneous UUID utility methods.
 *
 * @see UUID
 * @since 1.1
 */
public final class Uuids {

  /**
   * Creates a UUID from the standard string representation.
   *
   * <p>The UUID standard string representation is as described by this BNF:
   *
   * <blockquote>
   *
   * <pre>{@code
   * UUID                   = <time_low> "-"
   *                          <time_mid> "-"
   *                          <time_high_and_version> "-"
   *                          <variant_and_sequence> "-"
   *                          <node>
   * time_low               = 4*<hexOctet>
   * time_mid               = 2*<hexOctet>
   * time_high_and_version  = 2*<hexOctet>
   * variant_and_sequence   = 2*<hexOctet>
   * node                   = 6*<hexOctet>
   * hexOctet               = <hexDigit><hexDigit>
   * hexDigit               =
   *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
   *       | "a" | "b" | "c" | "d" | "e" | "f"
   *       | "A" | "B" | "C" | "D" | "E" | "F"
   * }</pre>
   *
   * </blockquote>
   *
   * @param str a string that specifies a UUID; not null
   * @return a UUID with the specified value
   * @throws IllegalArgumentException if str does not conform to the standard string representation
   * @since 1.1
   */
  public static UUID fromStandardRepresentationString(String str) {
    requireNonNull(str, "str");
    if (!(str.length() == UUID_STANDARD_LENGTH && UUID_STANDARD_PATTERN.matcher(str).matches())) {
      throw new IllegalArgumentException("Invalid UUID string: " + str);
    }

    long mostSigBits = Long.decode("0x" + str.substring(0, 8));
    mostSigBits <<= 16L;
    mostSigBits |= Long.decode("0x" + str.substring(9, 13));
    mostSigBits <<= 16L;
    mostSigBits |= Long.decode("0x" + str.substring(14, 18));

    long leastSigBits = Long.decode("0x" + str.substring(19, 23));
    leastSigBits <<= 48L;
    leastSigBits |= Long.decode("0x" + str.substring(24, 36));

    return new UUID(mostSigBits, leastSigBits);
  }

  /**
   * Creates a UUID from the shortened string representation.
   *
   * <p>The UUID shortened string representation is as described by this BNF:
   *
   * <blockquote>
   *
   * <pre>{@code
   * UUID                   = <time_low>
   *                          <time_mid>
   *                          <time_high_and_version>
   *                          <variant_and_sequence>
   *                          <node>
   * time_low               = 4*<hexOctet>
   * time_mid               = 2*<hexOctet>
   * time_high_and_version  = 2*<hexOctet>
   * variant_and_sequence   = 2*<hexOctet>
   * node                   = 6*<hexOctet>
   * hexOctet               = <hexDigit><hexDigit>
   * hexDigit               =
   *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
   *       | "a" | "b" | "c" | "d" | "e" | "f"
   *       | "A" | "B" | "C" | "D" | "E" | "F"
   * }</pre>
   *
   * </blockquote>
   *
   * @param str a string that specifies a UUID; not null
   * @return a UUID with the specified value
   * @throws IllegalArgumentException if str does not conform to the shortened string representation
   * @since 1.1
   */
  public static UUID fromShortenedRepresentationString(String str) {
    requireNonNull(str, "str");
    if (!(str.length() == UUID_SHORTENED_LENGTH && SHORTENED_UUID_PATTERN.matcher(str).matches())) {
      throw new IllegalArgumentException("Invalid UUID string: " + str);
    }

    long mostSigBits = Long.decode("0x" + str.substring(0, 8));
    mostSigBits <<= 16L;
    mostSigBits |= Long.decode("0x" + str.substring(8, 12));
    mostSigBits <<= 16L;
    mostSigBits |= Long.decode("0x" + str.substring(12, 16));

    long leastSigBits = Long.decode("0x" + str.substring(16, 20));
    leastSigBits <<= 48L;
    leastSigBits |= Long.decode("0x" + str.substring(20, 32));

    return new UUID(mostSigBits, leastSigBits);
  }

  /**
   * Returns a standard string representation of the given UUID.
   *
   * <p>The representation is as described by this BNF:
   *
   * <blockquote>
   *
   * <pre>{@code
   * UUID                   = <time_low> "-"
   *                          <time_mid> "-"
   *                          <time_high_and_version> "-"
   *                          <variant_and_sequence> "-"
   *                          <node>
   * time_low               = 4*<hexOctet>
   * time_mid               = 2*<hexOctet>
   * time_high_and_version  = 2*<hexOctet>
   * variant_and_sequence   = 2*<hexOctet>
   * node                   = 6*<hexOctet>
   * hexOctet               = <hexDigit><hexDigit>
   * hexDigit               =
   *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
   *       | "a" | "b" | "c" | "d" | "e" | "f"
   *       | "A" | "B" | "C" | "D" | "E" | "F"
   * }</pre>
   *
   * </blockquote>
   *
   * @param uuid a UUID; not null
   * @return a string representation of this UUID
   * @see UUID#toString()
   * @since 1.1
   */
  public static String toStandardRepresentationString(UUID uuid) {
    requireNonNull(uuid, "uuid");
    return uuid.toString();
  }

  /**
   * Returns a shortened string representation of the given UUID.
   *
   * <p>The representation is as described by this BNF:
   *
   * <blockquote>
   *
   * <pre>{@code
   * UUID                   = <time_low>
   *                          <time_mid>
   *                          <time_high_and_version>
   *                          <variant_and_sequence>
   *                          <node>
   * time_low               = 4*<hexOctet>
   * time_mid               = 2*<hexOctet>
   * time_high_and_version  = 2*<hexOctet>
   * variant_and_sequence   = 2*<hexOctet>
   * node                   = 6*<hexOctet>
   * hexOctet               = <hexDigit><hexDigit>
   * hexDigit               =
   *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
   *       | "a" | "b" | "c" | "d" | "e" | "f"
   *       | "A" | "B" | "C" | "D" | "E" | "F"
   * }</pre>
   *
   * </blockquote>
   *
   * @param uuid a UUID; not null
   * @return a string representation of this UUID
   * @since 1.1
   */
  public static String toShortenedRepresentationString(UUID uuid) {
    requireNonNull(uuid, "uuid");
    return format(
        Locale.ROOT, "%016x%016x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
  }

  private static final int UUID_STANDARD_LENGTH = 36;
  private static final Pattern UUID_STANDARD_PATTERN =
      Pattern.compile(
          "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", CASE_INSENSITIVE);

  private static final int UUID_SHORTENED_LENGTH = 32;
  private static final Pattern SHORTENED_UUID_PATTERN =
      Pattern.compile("^[0-9a-f]{32}$", CASE_INSENSITIVE);

  private Uuids() {
    // prevent instantiation
  }
}
