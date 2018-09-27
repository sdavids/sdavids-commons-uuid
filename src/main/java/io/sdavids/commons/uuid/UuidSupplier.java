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

import static java.security.AccessController.doPrivileged;
import static java.util.Objects.requireNonNull;
import static java.util.ServiceLoader.load;
import static java.util.UUID.randomUUID;
import static org.apiguardian.api.API.Status.STABLE;

import java.io.Serializable;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;
import org.apiguardian.api.API;

/**
 * Suppliers for UUIDs.
 *
 * <p>The default instance creates random UUIDs.
 *
 * <h3 id="usage">Example Usage and Test</h3>
 *
 * <h4 id="entity">Entity</h4>
 *
 * <pre><code>
 *   public class MyEntity {
 *
 *     private UUID uuid;
 *
 *     // ...
 *
 *     public UUID getUuid() {
 *       return uuid;
 *     }
 *
 *     public MyEntity create() {
 *       if (uuid != null) {
 *         throw new IllegalStateException("MyEntity already created");
 *       }
 *
 *       uuid = UuidSupplier.getDefault().get();
 *
 *       return this;
 *     }
 *   }
 * </code></pre>
 *
 * <h4 id="test">Test</h4>
 *
 * <pre><code>
 *   class MyEntityTest {
 *
 *     /// ---- Random UUIDs are used by default ----
 *
 *     private final Set&lt;UUID&gt; uuids = ConcurrentHashMap.newKeySet();
 *
 *    {@literal @}RepeatedTest(100)
 *     void randomDefault() {
 *       UUID uuid = new MyEntity().create().getUuid();
 *
 *       Assertions.assertFalse(uuids.contains(uuid));
 *
 *       uuids.add(uuid);
 *     }
 *
 *     /// ---- Always return a fixed UUID ----
 *
 *     static final UUID FIXED_UUID = UUID.fromString("3f0f2ddb-b2e9-4757-9348-80ed6057abb3");
 *
 *     public static class FixedUuidSupplier extends UuidSupplier {
 *
 *      {@literal @}Override
 *       public UUID get() {
 *         return FIXED_UUID;
 *       }
 *     }
 *
 *    {@literal @}RepeatedTest(100)
 *     void withFixed() {
 *       MockServices.withServicesForRunnableInCurrentThread(
 *           () -&gt; Assertions.assertEquals(FIXED_UUID, new MyEntity().create().getUuid()),
 *           FixedUuidSupplier.class);
 *     }
 *
 *     /// ---- Return UUIDs offered to a queue previously ----
 *
 *     static final UUID QUEUE_DEFAULT_UUID = UUID.fromString("0e2d51b3-a885-44eb-ba62-72039e5c5570");
 *
 *     static final ThreadLocal&lt;Queue&lt;UUID&gt;&gt; threadLocalQueue =
 *         ThreadLocal.withInitial(ConcurrentLinkedQueue::new);
 *
 *     public static class QueueBasedUuidSupplier extends UuidSupplier {
 *
 *       private final Supplier&lt;UUID&gt; delegateSupplier;
 *
 *       public QueueBasedUuidSupplier() {
 *         delegateSupplier =
 *             UuidSupplier.queueBasedUuidSupplier(threadLocalQueue.get(), QUEUE_DEFAULT_UUID);
 *       }
 *
 *      {@literal @}Override
 *       public UUID get() {
 *         return delegateSupplier.get();
 *       }
 *     }
 *
 *    {@literal @}Test
 *     void queueBased() {
 *       MockServices.withServicesForRunnableInCurrentThread(
 *           () -&gt; {
 *             UUID uuid1 = UUID.fromString("80f99fd6-1ca8-45d5-a406-dcd8b9293fe8");
 *             UUID uuid2 = UUID.fromString("ffec4ea6-e28b-49f2-b84b-9be0854d6077");
 *             UUID uuid3 = UUID.fromString("bdf730b5-306b-4b56-b7a9-e29781e941df");
 *             UUID uuid4 = UUID.fromString("819753ad-d485-4cd4-8e74-0edc17ade79f");
 *
 *             threadLocalQueue.get().offer(uuid1);
 *             threadLocalQueue.get().offer(uuid2);
 *             threadLocalQueue.get().offer(uuid3);
 *             try {
 *               Assertions.assertEquals(uuid1, new MyEntity().create().getUuid());
 *
 *               Assertions.assertEquals(uuid2, UuidSupplier.getDefault().get());
 *
 *               Assertions.assertEquals(uuid3, new MyEntity().create().getUuid());
 *
 *               Assertions.assertEquals(QUEUE_DEFAULT_UUID, new MyEntity().create().getUuid());
 *               Assertions.assertEquals(QUEUE_DEFAULT_UUID, UuidSupplier.getDefault().get());
 *
 *               threadLocalQueue.get().offer(uuid4);
 *               Assertions.assertEquals(uuid4, new MyEntity().create().getUuid());
 *
 *               Assertions.assertEquals(QUEUE_DEFAULT_UUID, UuidSupplier.getDefault().get());
 *             } finally {
 *               threadLocalQueue.remove();
 *             }
 *           },
 *           QueueBasedUuidSupplier.class);
 *     }
 *   }
 * </code></pre>
 *
 * <h4 id="junit-platform">JUnit 5 Configuration</h4>
 *
 * src/test/resources/junit-platform.properties
 *
 * <pre><code>
 *   junit.jupiter.execution.parallel.enabled=true
 * </code></pre>
 *
 * <h4 id="maven">Maven Configuration</h4>
 *
 * <p>pom.xml
 *
 * <pre><code>
 *   &lt;project&gt;
 *     &lt;dependencies&gt;
 *       &lt;dependency&gt;
 *         &lt;groupId&gt;io.sdavids.commons&lt;/groupId&gt;
 *         &lt;artifactId&gt;sdavids-commons-uuid&lt;/artifactId&gt;
 *         &lt;version&gt;...&lt;/version&gt;
 *       &lt;/dependency&gt;
 *       &lt;dependency&gt;
 *         &lt;groupId&gt;org.junit.jupiter&lt;/groupId&gt;
 *         &lt;artifactId&gt;junit-jupiter-engine&lt;/artifactId&gt;
 *         &lt;version&gt;...&lt;/version&gt;
 *         &lt;scope&gt;test&lt;/scope&gt;
 *       &lt;/dependency&gt;
 *       &lt;dependency&gt;
 *         &lt;groupId&gt;io.sdavids.commons.test&lt;/groupId&gt;
 *         &lt;artifactId&gt;sdavids-commons-test&lt;/artifactId&gt;
 *         &lt;version&gt;...&lt;/version&gt;
 *         &lt;scope&gt;test&lt;/scope&gt;
 *       &lt;/dependency&gt;
 *     &lt;/dependencies&gt;
 *
 *     &lt;build&gt;
 *       &lt;plugins&gt;
 *         &lt;plugin&gt;
 *           &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
 *           &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
 *           &lt;version&gt;...&lt;/version&gt;
 *           &lt;configuration&gt;
 *             &lt;systemPropertyVariables&gt;
 *               &lt;io.sdavids.commons.uuid.uuid.supplier.default.cached&gt;false&lt;/io.sdavids.commons.uuid.uuid.supplier.default.cached&gt;
 *             &lt;/systemPropertyVariables&gt;
 *           &lt;/configuration&gt;
 *         &lt;/plugin&gt;
 *       &lt;/plugin&gt;
 *     &lt;/build&gt;
 *   &lt;/project&gt;
 * </code></pre>
 *
 * <h4 id="gradle">Gradle Configuration</h4>
 *
 * <p>build.gradle
 *
 * <pre><code>
 *   dependencies {
 *     implementation 'io.sdavids.commons:sdavids-commons-uuid:...'
 *
 *     testImplementation 'org.junit.jupiter:junit-jupiter-engine:...'
 *     testImplementation 'io.sdavids.commons.test:sdavids-commons-test:...'
 *   }
 *
 *   tasks.withType(Test).configureEach {
 *     useJUnitPlatform()
 *
 *     // Turn off caching of retrieved UuidSupplier
 *     systemProperty 'io.sdavids.commons.uuid.uuid.supplier.default.cached', 'false'
 *
 *     // Gradle Parallel Test Execution
 *     maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
 *     forkEvery = 1
 *   }
 * </code></pre>
 *
 * @see UUID#randomUUID()
 * @see java.util.ServiceLoader
 * @see <a href="http://wiki.apidesign.org/wiki/Injectable_Singleton">Injectable Singleton</a>
 * @since 1.0
 */
@API(status = STABLE, since = "1.0")
public abstract class UuidSupplier implements Supplier<UUID> {

  static final String CACHED_PROPERTY_KEY = "io.sdavids.commons.uuid.uuid.supplier.default.cached";

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

  private static final class NonCachingUuidSupplier implements Supplier<UUID>, Serializable {

    private static final long serialVersionUID = 166573036614018397L;

    private volatile String supplierClassName = "unitialized - call get() first";

    @Override
    public UUID get() {
      Iterator<UuidSupplier> providers = load(UuidSupplier.class).iterator();

      Supplier<UUID> supplier =
          providers.hasNext() ? providers.next() : RandomUuidSupplier.INSTANCE;

      supplierClassName = supplier.getClass().getName();

      return supplier.get();
    }

    @Override
    public String toString() {
      return "NonCachingUuidSupplier(" + supplierClassName + ')';
    }
  }

  private static final class SingletonHolder {

    private static Supplier<UUID> initialize() {
      String cached = getCachedSystemProperty();

      if (cached == null || Boolean.parseBoolean(cached)) {
        Iterator<UuidSupplier> providers = load(UuidSupplier.class).iterator();

        return providers.hasNext() ? providers.next() : RandomUuidSupplier.INSTANCE;
      }

      return new NonCachingUuidSupplier();
    }

    private static String getCachedSystemProperty() {
      return System.getSecurityManager() == null
          ? System.getProperty(CACHED_PROPERTY_KEY)
          : doPrivileged((PrivilegedAction<String>) () -> System.getProperty(CACHED_PROPERTY_KEY));
    }

    static final Supplier<UUID> INSTANCE = initialize();
  }

  /**
   * Obtains the default instance of the UUID supplier.
   *
   * <p>The first instance of type {@code UuidSupplier} obtained by the {@code ServiceLoader} is
   * used. Otherwise, a supplier returning random UUIDs is used.
   *
   * <p>By default the instance returned by the {@code ServiceLoader} is cached. In order to turn
   * off caching set the system property {@code
   * io.sdavids.commons.uuid.uuid.supplier.default.cached} to {@code false}. <em>Note:</em> The
   * system property is evaluated once, i.e. caching cannot be dynamically enabled/disabled.
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
  @API(status = STABLE, since = "1.1")
  public static Supplier<UUID> queueBasedUuidSupplier(Queue<UUID> uuidQueue, UUID emptyQueueValue) {
    return new QueueBasedUuidSupplier(uuidQueue, emptyQueueValue);
  }

  protected UuidSupplier() {
    // injectable singleton
  }
}
