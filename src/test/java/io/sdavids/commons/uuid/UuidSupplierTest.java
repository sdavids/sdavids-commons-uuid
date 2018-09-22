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
import static io.sdavids.commons.uuid.TestableUuidSupplier.UUID_1;
import static io.sdavids.commons.uuid.TestableUuidSupplier.UUID_2;
import static io.sdavids.commons.uuid.TestableUuidSupplier.UUID_3;
import static io.sdavids.commons.uuid.TestableUuidSupplier.UUID_4;
import static io.sdavids.commons.uuid.UuidSupplier.fixedUuidSupplier;
import static io.sdavids.commons.uuid.UuidSupplier.queueBasedUuidSupplier;
import static io.sdavids.commons.uuid.UuidSupplier.randomUuidSupplier;
import static java.util.ServiceLoader.load;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.generate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

final class UuidSupplierTest {

  private static final long COUNT = 1000L;

  private static Function<Future<UUID>, UUID> getFuture() {
    return f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new IllegalStateException(e);
      }
    };
  }

  @Test
  void fixedUuidSupplier_null() {
    assertThrows(NullPointerException.class, () -> fixedUuidSupplier(null), "uuid");
  }

  @Test
  void fixedUuidSupplier_() throws InterruptedException {
    Supplier<UUID> supplier = fixedUuidSupplier(FIXED_UUID);

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Set<UUID> uuids = result.stream().map(getFuture()).collect(toSet());

    assertThat(uuids).containsExactly(FIXED_UUID);
  }

  @Test
  void randomUuidSupplier_() throws InterruptedException {
    Supplier<UUID> supplier = randomUuidSupplier();

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Set<UUID> uuids = result.stream().map(getFuture()).collect(toSet());

    assertThat(uuids).hasSize((int) COUNT);
    assertThat(uuids).doesNotContainNull();
  }

  @Test
  void queueBasedUuidSupplier_queue_empty_one_single_thread() {
    Queue<UUID> queue = new ArrayDeque<>();

    Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
  }

  @Test
  void queueBasedUuidSupplier_queue_one_single_thread() {
    Queue<UUID> queue = new ArrayDeque<>();

    queue.offer(UUID_1);

    Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

    assertThat(supplier.get()).isEqualTo(UUID_1);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
  }

  @Test
  void queueBasedUuidSupplier_queue_many_single_thread() {
    Queue<UUID> queue = new ArrayDeque<>();

    Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

    assertThat(supplier.get()).isEqualTo(FIXED_UUID);

    queue.offer(UUID_1);
    queue.offer(UUID_2);
    queue.offer(UUID_3);

    assertThat(supplier.get()).isEqualTo(UUID_1);
    assertThat(supplier.get()).isEqualTo(UUID_2);
    assertThat(supplier.get()).isEqualTo(UUID_3);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);

    queue.offer(UUID_4);

    assertThat(supplier.get()).isEqualTo(UUID_4);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    assertThat(supplier.get()).isEqualTo(FIXED_UUID);
  }

  @Test
  void queueBasedUuidSupplier_queue_empty_multi_threaded() throws InterruptedException {
    Queue<UUID> queue = new ArrayDeque<>();

    Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Set<UUID> uuids = result.stream().map(getFuture()).collect(toSet());

    assertThat(uuids).containsExactly(FIXED_UUID);
  }

  @Test
  void queueBasedUuidSupplier_queue_one_multi_threaded() throws InterruptedException {
    Queue<UUID> queue = new ConcurrentLinkedQueue<>();

    queue.offer(UUID_1);

    Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Map<UUID, Long> uuids =
        result.stream().map(getFuture()).collect(groupingBy(identity(), counting()));

    assertThat(uuids).hasSize(2);
    assertThat(uuids.get(FIXED_UUID)).isEqualTo(COUNT - 1L);
    assertThat(uuids.get(UUID_1)).isEqualTo(1L);
  }

  @Test
  void queueBasedUuidSupplier_queue_many_multi_threaded() throws InterruptedException {
    Queue<UUID> queue = new ConcurrentLinkedQueue<>();

    queue.offer(UUID_1);
    queue.offer(UUID_2);
    queue.offer(UUID_3);

    Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

    ExecutorService service = newFixedThreadPool(5);

    List<Future<UUID>> result =
        service.invokeAll(
            generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

    service.shutdown();
    service.awaitTermination(1L, MINUTES);

    Map<UUID, Long> uuids =
        result.stream().map(getFuture()).collect(groupingBy(identity(), counting()));

    assertThat(uuids).hasSize(4);
    assertThat(uuids.get(FIXED_UUID)).isEqualTo(COUNT - 3L);
    assertThat(uuids.get(UUID_1)).isEqualTo(1L);
    assertThat(uuids.get(UUID_2)).isEqualTo(1L);
    assertThat(uuids.get(UUID_3)).isEqualTo(1L);
  }

  @Test
  void getDefault_default_impl() {
    Iterator<UuidSupplier> providers = load(UuidSupplier.class).iterator();

    assertThat(providers.hasNext()).isFalse();

    Supplier<UUID> first = UuidSupplier.getDefault();

    assertThat(first).isNotNull();

    Supplier<UUID> second = UuidSupplier.getDefault();

    assertThat(second).isNotNull();

    assertThat(first).isSameAs(second);
  }

  @Test
  void getDefault_get() {
    assertThat(UuidSupplier.getDefault().get()).isNotNull();
  }
}
