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

import static io.sdavids.commons.uuid.TestableUuidSupplier.FIXED_UUID;
import static io.sdavids.commons.uuid.UuidSupplier.fixedUuidSupplier;
import static io.sdavids.commons.uuid.UuidSupplier.randomUuidSupplier;
import static java.util.ServiceLoader.load;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.generate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.lambda.Unchecked.function;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <strong>Important</strong>: This test has to run in a new VM.
 *
 * <h3 id="Gradle">Gradle</h3>
 *
 * <pre>
 * <code>test {
 *  forkEvery 1
 * }</code></pre>
 */
public final class UuidSupplierTest {

  private static final long COUNT = 1000L;

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void fixedUuidSupplier_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("uuid");

    //noinspection ConstantConditions
    fixedUuidSupplier(null);
  }

  @Test
  public void fixedUuidSupplier_() throws InterruptedException {
    Supplier<UUID> supplier = fixedUuidSupplier(FIXED_UUID);

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Set<UUID> uuids = result.stream().map(function(Future::get)).collect(toSet());

    assertThat(uuids).containsExactly(FIXED_UUID);
  }

  @Test
  public void randomUuidSupplier_() throws InterruptedException {
    Supplier<UUID> supplier = randomUuidSupplier();

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Set<UUID> uuids = result.stream().map(function(Future::get)).collect(toSet());

    assertThat(uuids).hasSize((int) COUNT);
    assertThat(uuids).doesNotContainNull();
  }

  @Test
  public void getDefault_default_impl() {
    Iterator<UuidSupplier> providers = load(UuidSupplier.class).iterator();

    assertThat(providers.hasNext()).isFalse();

    Supplier<UUID> first = UuidSupplier.getDefault();

    assertThat(first).isNotNull();

    Supplier<UUID> second = UuidSupplier.getDefault();

    assertThat(second).isNotNull();

    assertThat(first).isSameAs(second);
  }

  @Test
  public void getDefault_get() {
    assertThat(UuidSupplier.getDefault().get()).isNotNull();
  }
}
