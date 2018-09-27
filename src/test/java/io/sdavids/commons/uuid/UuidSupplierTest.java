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

import static io.sdavids.commons.test.MockServices.withServicesForRunnableInCurrentThread;
import static io.sdavids.commons.uuid.TestableUuidSupplier.FIXED_UUID;
import static io.sdavids.commons.uuid.UuidSupplier.fixedUuidSupplier;
import static io.sdavids.commons.uuid.UuidSupplier.queueBasedUuidSupplier;
import static io.sdavids.commons.uuid.UuidSupplier.randomUuidSupplier;
import static java.util.UUID.fromString;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.generate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayDeque;
import java.util.LinkedList;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"ClassCanBeStatic", "PMD.UseUtilityClass"})
class UuidSupplierTest {

  @Nested
  class FixedUuidSupplier {

    @Test
    void withUuidNull() {
      assertThrows(NullPointerException.class, () -> fixedUuidSupplier(null), "uuid");
    }

    @Test
    void returnsFixed() throws InterruptedException {
      Supplier<UUID> supplier = fixedUuidSupplier(FIXED_UUID);

      ExecutorService executorService = newFixedThreadPool(5);

      List<Future<UUID>> result =
          executorService.invokeAll(
              generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

      shutdownExecutorService(executorService);

      Set<UUID> uuids = result.stream().map(getFuture()).collect(toSet());

      assertThat(uuids).containsExactly(FIXED_UUID);
    }
  }

  @Nested
  class RandomUuidSupplier {

    @Test
    void returnsRandom() throws InterruptedException {
      Supplier<UUID> supplier = randomUuidSupplier();

      ExecutorService executorService = newFixedThreadPool(5);

      List<Future<UUID>> result =
          executorService.invokeAll(
              generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

      shutdownExecutorService(executorService);

      Set<UUID> uuids = result.stream().map(getFuture()).collect(toSet());

      assertThat(uuids).hasSize((int) COUNT);
      assertThat(uuids).doesNotContainNull();
    }
  }

  @Nested
  class QueueBasedUuidSupplier {

    @Test
    void withEmptyQueueSingleThread() {
      Queue<UUID> queue = new ArrayDeque<>();

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    }

    @Test
    void withNullElementQueueSingleThread() {
      @SuppressWarnings("JdkObsolete")
      Queue<UUID> queue = new LinkedList<>();

      queue.offer(null);

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    }

    @Test
    void withOneElementQueueSingleThread() {
      Queue<UUID> queue = new ArrayDeque<>();

      queue.offer(UUID_1);

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      assertThat(supplier.get()).isEqualTo(UUID_1);
      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
      assertThat(supplier.get()).isEqualTo(FIXED_UUID);
    }

    @Test
    void withManyElementsQueueSingleThread() {
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
    void withEmptyQueueMultiThreaded() throws InterruptedException {
      Queue<UUID> queue = new ArrayDeque<>();

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      ExecutorService executorService = newFixedThreadPool(5);

      List<Future<UUID>> result =
          executorService.invokeAll(
              generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

      Set<UUID> uuids = result.stream().map(getFuture()).collect(toSet());

      assertThat(uuids).containsExactly(FIXED_UUID);

      shutdownExecutorService(executorService);
    }

    @Test
    void withNullElementQueueMultiThreaded() throws InterruptedException {
      Queue<UUID> queue =
          new ConcurrentLinkedQueue<UUID>() {

            private final transient AtomicBoolean first = new AtomicBoolean(true);

            @Override
            public UUID poll() {
              if (first.compareAndSet(true, false)) {
                return null;
              }
              return super.poll();
            }
          };

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      ExecutorService executorService = newFixedThreadPool(5);

      List<Future<UUID>> result =
          executorService.invokeAll(
              generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

      Map<UUID, Long> uuids =
          result.stream().map(getFuture()).collect(groupingBy(identity(), counting()));

      assertThat(uuids).hasSize(1);
      assertThat(uuids.get(FIXED_UUID)).isEqualTo(COUNT);

      shutdownExecutorService(executorService);
    }

    @Test
    void withOneElementQueueMultiThreaded() throws InterruptedException {
      Queue<UUID> queue = new ConcurrentLinkedQueue<>();

      queue.offer(UUID_1);

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      ExecutorService executorService = newFixedThreadPool(5);

      List<Future<UUID>> result =
          executorService.invokeAll(
              generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

      Map<UUID, Long> uuids =
          result.stream().map(getFuture()).collect(groupingBy(identity(), counting()));

      assertThat(uuids).hasSize(2);
      assertThat(uuids.get(FIXED_UUID)).isEqualTo(COUNT - 1L);
      assertThat(uuids.get(UUID_1)).isEqualTo(1L);

      shutdownExecutorService(executorService);
    }

    @Test
    void withManyElementsQueueMultiThreaded() throws InterruptedException {
      Queue<UUID> queue = new ConcurrentLinkedQueue<>();

      queue.offer(UUID_1);
      queue.offer(UUID_2);
      queue.offer(UUID_3);

      Supplier<UUID> supplier = queueBasedUuidSupplier(queue, FIXED_UUID);

      ExecutorService executorService = newFixedThreadPool(5);

      List<Future<UUID>> result =
          executorService.invokeAll(
              generate(() -> (Callable<UUID>) supplier::get).limit(COUNT).collect(toList()));

      Map<UUID, Long> uuids =
          result.stream().map(getFuture()).collect(groupingBy(identity(), counting()));

      assertThat(uuids).hasSize(4);
      assertThat(uuids.get(FIXED_UUID)).isEqualTo(COUNT - 3L);
      assertThat(uuids.get(UUID_1)).isEqualTo(1L);
      assertThat(uuids.get(UUID_2)).isEqualTo(1L);
      assertThat(uuids.get(UUID_3)).isEqualTo(1L);

      shutdownExecutorService(executorService);
    }
  }

  @Nested
  class GetDefault {

    @Test
    void returnsDefaultImpl() {
      Supplier<UUID> first = UuidSupplier.getDefault();

      assertThat(first).isNotNull();

      UUID firstUuid = first.get();

      assertThat(firstUuid).isNotNull();

      Supplier<UUID> second = UuidSupplier.getDefault();

      assertThat(second).isNotNull();

      UUID secondUuid = second.get();

      assertThat(firstUuid).isNotEqualTo(secondUuid);

      assertThat(first).isSameAs(second);
    }

    @Test
    void returnsRegisteredImpl() {
      withServicesForRunnableInCurrentThread(
          () -> {
            Supplier<UUID> first = UuidSupplier.getDefault();

            assertThat(first).isNotNull();

            UUID firstUuid = first.get();

            assertThat(first.toString())
                .isEqualTo("NonCachingUuidSupplier(" + TestableUuidSupplier.class.getName() + ')');
            assertThat(firstUuid).isEqualTo(FIXED_UUID);

            Supplier<UUID> second = UuidSupplier.getDefault();

            assertThat(second).isNotNull();

            UUID secondUuid = second.get();

            assertThat(second.toString())
                .isEqualTo("NonCachingUuidSupplier(" + TestableUuidSupplier.class.getName() + ')');
            assertThat(secondUuid).isEqualTo(FIXED_UUID);

            assertThat(first).isSameAs(second);
          },
          TestableUuidSupplier.class);
    }
  }

  static final UUID UUID_1 = fromString("1f0f2ddb-b2e9-4757-9348-80ed6057abb1");
  static final UUID UUID_2 = fromString("1f0f2ddb-b2e9-4757-9348-80ed6057abb2");
  static final UUID UUID_3 = fromString("1f0f2ddb-b2e9-4757-9348-80ed6057abb3");
  static final UUID UUID_4 = fromString("1f0f2ddb-b2e9-4757-9348-80ed6057abb4");

  static final long COUNT = 1000L;

  static Function<Future<UUID>, UUID> getFuture() {
    return f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new IllegalStateException(e);
      }
    };
  }

  static void shutdownExecutorService(ExecutorService executorService) {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(30L, SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }
}
