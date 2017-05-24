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

import static java.util.UUID.fromString;

import java.util.UUID;

public final class TestableUuidSupplier extends UuidSupplier {

  public static final UUID FIXED_UUID = fromString("3f0f2ddb-b2e9-4757-9348-80ed6057abb3");

  @Override
  public UUID get() {
    return FIXED_UUID;
  }
}
