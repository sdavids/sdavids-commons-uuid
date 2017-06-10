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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
import java.util.Locale;
import java.util.UUID;

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
  @SuppressWarnings({"MagicCharacter", "MagicNumber", "StringConcatenationMissingWhitespace"})
  public static UUID fromStandardRepresentationString(String str) {
    requireNonNull(str, "str");
    checkArgument(str.length() == UUID_STANDARD_LENGTH, "Invalid UUID string: %s", str);
    checkArgument(str.charAt(8) == '-', "Invalid UUID string: %s", str);
    checkArgument(str.charAt(13) == '-', "Invalid UUID string: %s", str);
    checkArgument(str.charAt(18) == '-', "Invalid UUID string: %s", str);
    checkArgument(str.charAt(23) == '-', "Invalid UUID string: %s", str);
    checkArgument(
        DASH_MATCHER.countIn(str) == UUID_STANDARD_DASH_COUNT, "Invalid UUID string: %s", str);
    checkArgument(UUID_STANDARD_MATCHER.matchesAllOf(str), "Invalid UUID string: %s", str);

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
  @SuppressWarnings({"MagicCharacter", "MagicNumber", "StringConcatenationMissingWhitespace"})
  public static UUID fromShortenedRepresentationString(String str) {
    requireNonNull(str, "str");
    checkArgument(str.length() == UUID_SHORTENED_LENGTH, "Invalid UUID string: %s", str);
    checkArgument(UUID_SHORTENED_MATCHER.matchesAllOf(str), "Invalid UUID string: %s", str);

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

  private static final int UUID_STANDARD_DASH_COUNT = 4;
  private static final int UUID_STANDARD_LENGTH = 36;
  private static final int UUID_SHORTENED_LENGTH = UUID_STANDARD_LENGTH - UUID_STANDARD_DASH_COUNT;

  private static final CharMatcher UUID_STANDARD_MATCHER =
      CharMatcher.anyOf("-0123456789ABCDEFabcdef");
  private static final CharMatcher UUID_SHORTENED_MATCHER =
      CharMatcher.anyOf("0123456789ABCDEFabcdef");
  private static final CharMatcher DASH_MATCHER = CharMatcher.is('-');

  private Uuids() {
    // prevent instantiation
  }
}
