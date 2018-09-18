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

import static java.util.Objects.requireNonNull;
import static java.util.ServiceLoader.load;
import static java.util.UUID.randomUUID;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Suppliers for UUIDs.
 *
 * <p>The default instance creates random UUIDs.
 *
 * @see UUID#randomUUID()
 * @see java.util.ServiceLoader
 * @see <a href="http://wiki.apidesign.org/wiki/Injectable_Singleton">Injectable Singleton</a>
 * @since 1.0
 */
@SuppressWarnings("CyclicClassDependency")
public abstract class UuidSupplier implements Supplier<UUID> {

  private enum RandomUuidSupplier implements Supplier<UUID> {
    INSTANCE;

    @Override
    public String toString() {
      return "UuidSupplier.randomUuidSupplier()";
    }

    @Override
    public UUID get() {
      return randomUUID();
    }
  }

  private static final class FixedUuidSupplier implements Supplier<UUID>, Serializable {

    private static final long serialVersionUID = 1665510966140281397L;

    private final UUID uuid;

    FixedUuidSupplier(UUID uuid) {
      this.uuid = requireNonNull(uuid, "uuid");
    }

    @Override
    public String toString() {
      return "UuidSupplier.fixedUuidSupplier()";
    }

    @Override
    public UUID get() {
      return uuid;
    }
  }

  private static final class QueueBasedUuidSupplier implements Supplier<UUID>, Serializable {

    private static final long serialVersionUID = -5396596456485687697L;

    private final Queue<UUID> uuidQueue;
    private final UUID emptyQueueValue;

    QueueBasedUuidSupplier(Queue<UUID> uuidQueue, UUID emptyQueueValue) {
      this.uuidQueue = requireNonNull(uuidQueue, "uuidQueue");
      this.emptyQueueValue = requireNonNull(emptyQueueValue, "emptyQueueValue");
    }

    @Override
    public String toString() {
      return "QueueBasedUuidSupplier.queueBasedUuidSupplier()";
    }

    @Override
    public UUID get() {
      UUID uuid = uuidQueue.poll();
      return uuid == null ? emptyQueueValue : uuid;
    }
  }

  @SuppressWarnings("CyclicClassDependency")
  private static final class SingletonHolder {

    private static Supplier<UUID> initialize() {
      Iterator<UuidSupplier> providers = load(UuidSupplier.class).iterator();

      return providers.hasNext() ? providers.next() : RandomUuidSupplier.INSTANCE;
    }

    static final Supplier<UUID> INSTANCE = initialize();
  }

  /**
   * Obtains the default instance of the UUID supplier.
   *
   * <p>The first instance of type {@code UuidSupplier} obtained by the {@code ServiceLoader} is
   * used. Otherwise, a supplier returning random UUIDs is used.
   *
   * @return some UUID supplier; never null
   * @see #randomUuidSupplier()
   * @since 1.0
   */
  public static Supplier<UUID> getDefault() {
    return SingletonHolder.INSTANCE;
  }

  /**
   * Returns a supplier returning random UUIDs.
   *
   * @return a random UUID supplier
   * @since 1.0
   */
  public static Supplier<UUID> randomUuidSupplier() {
    return RandomUuidSupplier.INSTANCE;
  }

  /**
   * Returns a supplier returning a fixed UUID.
   *
   * @param uuid the UUID to be returned by the supplier; not null
   * @return a fixed UUID supplier
   * @since 1.0
   */
  public static Supplier<UUID> fixedUuidSupplier(UUID uuid) {
    return new FixedUuidSupplier(uuid);
  }

  /**
   * Returns a supplier returning UUIDs from a queue.
   *
   * @param uuidQueue the queue containing UUIDs to be returned by the supplier; not null
   * @param emptyQueueValue the UUID to be returned by the supplier when {@code uuidQueue} is empty;
   *     not null
   * @return a queue-based UUID supplier
   * @since 1.1
   */
  public static Supplier<UUID> queueBasedUuidSupplier(Queue<UUID> uuidQueue, UUID emptyQueueValue) {
    return new QueueBasedUuidSupplier(uuidQueue, emptyQueueValue);
  }

  protected UuidSupplier() {
    // injectable singleton
  }
}
