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

import static io.sdavids.commons.test.MockServices.setServices;
import static io.sdavids.commons.uuid.TestableUuidSupplier.FIXED_UUID;
import static java.util.ServiceLoader.load;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.BeforeClass;
import org.junit.Test;

// Important: This test has to run in a forked VM.
//
// IntelliJ:
//
//   Forkmode - class
//
// Gradle:
//
//   test {
//     forkEvery 1
//   }
public final class UuidSupplierWithServiceTest {

  @BeforeClass
  public static void setUp() {
    setServices(TestableUuidSupplier.class);
  }

  @Test
  public void getDefault_() {
    Iterator<UuidSupplier> providers = load(UuidSupplier.class).iterator();

    assertThat(providers.hasNext()).isTrue();

    Supplier<UUID> first = UuidSupplier.getDefault();

    assertThat(first).isNotNull();
    assertThat(first).isInstanceOf(TestableUuidSupplier.class);

    setServices();

    providers = load(UuidSupplier.class).iterator();

    assertThat(providers.hasNext()).isFalse();

    Supplier<UUID> second = UuidSupplier.getDefault();

    assertThat(second).isNotNull();
    assertThat(second).isInstanceOf(TestableUuidSupplier.class);

    assertThat(first).isSameAs(second);
  }

  @Test
  public void getDefault_get() {
    Supplier<UUID> supplier = UuidSupplier.getDefault();

    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
  }
}
