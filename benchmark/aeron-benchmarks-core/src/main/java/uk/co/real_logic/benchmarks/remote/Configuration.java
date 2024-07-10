/*
 * Copyright 2015-2024 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.benchmarks.remote;

import org.HdrHistogram.ValueRecorder;
import org.agrona.AsciiEncoding;
import org.agrona.AsciiNumberFormatException;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.NanoClock;
import org.agrona.concurrent.NoOpIdleStrategy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import static java.lang.System.getProperty;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.util.Objects.requireNonNull;
import static org.agrona.BitUtil.SIZE_OF_LONG;
import static org.agrona.BitUtil.toHex;
import static org.agrona.Strings.isEmpty;

/**
 * {@code Configuration} contains configuration values for the harness.
 * <p>
 * A {@code Configuration} instance can be created using the {@link Builder} class, e.g.:
 * <pre>
 *    final Configuration.Builder builder = new Configuration.Builder();
 *    builder.messageRate(1000);
 *    ...
 *    final Configuration configuration = builder.build();
 * </pre>
 * </p>
 */
public final class Configuration
{
    /**
     * Default number of the warmup iterations.
     */
    public static final int DEFAULT_WARMUP_ITERATIONS = 10;

    /**
     * Default number of the messages to be sent during the warmup iterations.
     */
    public static final int DEFAULT_WARMUP_MESSAGE_RATE = 10_000;

    /**
     * Default number of measurement iterations.
     */
    public static final int DEFAULT_ITERATIONS = 10;

    /**
     * Default number of messages in a single batch.
     */
    public static final int DEFAULT_BATCH_SIZE = 1;

    /**
     * Default tracking of latency history
     */
    public static final boolean DEFAULT_TRACK_HISTORY = false;

    /**
     * Minimal length in bytes of a single message. Contains enough space to hold a {@code timestamp} and a
     * {@code checksum}, i.e. two {@code long} values.
     */
    public static final int MIN_MESSAGE_LENGTH = 2 * SIZE_OF_LONG;

    /**
     * Name of the system property to configure the number of warmup iterations. Default value is
     * {@link #DEFAULT_WARMUP_ITERATIONS}.
     *
     * @see #warmupIterations()
     */
    public static final String WARMUP_ITERATIONS_PROP_NAME = "uk.co.real_logic.benchmarks.remote.warmup.iterations";

    /**
     * Name of the system property to configure the number of measurement iterations. Default value is
     * {@link #DEFAULT_ITERATIONS}.
     *
     * @see #iterations()
     */
    public static final String ITERATIONS_PROP_NAME = "uk.co.real_logic.benchmarks.remote.iterations";

    /**
     * Name of the required system property to configure the number of messages to be sent during the measurement
     * iterations.
     *
     * @see #messageRate()
     */
    public static final String MESSAGE_RATE_PROP_NAME = "uk.co.real_logic.benchmarks.remote.message.rate";

    /**
     * Name of the optional system property to configure the number of messages to be sent during the warmup
     * iterations.
     *
     * @see #warmupMessageRate()
     */
    public static final String WARMUP_MESSAGE_RATE_PROP_NAME = "uk.co.real_logic.benchmarks.remote.warmup.message.rate";

    /**
     * Name of the system property to configure the batch size, i.e. number of messages to be sent in a single burst.
     * Default value is {@link #DEFAULT_BATCH_SIZE}.
     *
     * @see #batchSize()
     */
    public static final String BATCH_SIZE_PROP_NAME = "uk.co.real_logic.benchmarks.remote.batch.size";

    /**
     * Name of the system property to configure the message size in bytes. Default value is {@link #MIN_MESSAGE_LENGTH}.
     *
     * @see #messageLength()
     */
    public static final String MESSAGE_LENGTH_PROP_NAME = "uk.co.real_logic.benchmarks.remote.message.length";

    /**
     * Name of the system property to configure the {@link IdleStrategy} to use when sending and receiving messages.
     * Must be a fully qualified class name. Default value is {@link NoOpIdleStrategy}.
     *
     * @see #idleStrategy()
     */
    public static final String IDLE_STRATEGY_PROP_NAME = "uk.co.real_logic.benchmarks.remote.idle.strategy";

    /**
     * Name of the required system property to configure the {@link MessageTransceiver} class (i.e. system under test)
     * to be used for the benchmark. Must be a fully qualified class name.
     */
    public static final String MESSAGE_TRANSCEIVER_PROP_NAME = "uk.co.real_logic.benchmarks.remote.message.transceiver";

    /**
     * Name of the system property to configure the output directory where histogram files for each run should be
     * stored. Default value is {@code results} directory created in the current directory.
     */
    public static final String OUTPUT_DIRECTORY_PROP_NAME = "uk.co.real_logic.benchmarks.remote.output.directory";

    /**
     * Name of the required system property to configure the output file name prefix.
     */
    public static final String OUTPUT_FILE_NAME_PROP_NAME = "uk.co.real_logic.benchmarks.remote.output.file";

    /**
     * Name of property to indicate tracking of latency history
     */
    public static final String TRACK_HISTORY_PROP_NAME = "uk.co.real_logic.benchmarks.remote.track.history";

    /**
     * Max message rate allowed, i.e. 1 message per nanosecond.
     */
    public static final int MAX_MESSAGE_RATE = 1_000_000_000;

    private static final int MAX_K_VALUE = Integer.MAX_VALUE / 1000;
    private static final int MAX_M_VALUE = Integer.MAX_VALUE / 1_000_000;
    private static final String LOGS_DIR = "logs";
    private static final MessageDigest SHA256;

    static
    {
        try
        {
            SHA256 = MessageDigest.getInstance("SHA-256");
        }
        catch (final NoSuchAlgorithmException ex)
        {
            throw new Error(ex);
        }
    }

    private final int warmupIterations;
    private final int iterations;
    private final int warmupMessageRate;
    private final int messageRate;
    private final int batchSize;
    private final int messageLength;
    private final Class<? extends MessageTransceiver> messageTransceiverClass;
    private final IdleStrategy idleStrategy;
    private final Path outputDirectory;
    private final Path logsDir;
    private final String rate;
    private final String outputFileNamePrefix;
    private final boolean trackHistory;

    private Configuration(final Builder builder)
    {
        this.warmupIterations = checkValueRange(
            builder.warmupIterations, 0, Integer.MAX_VALUE, WARMUP_ITERATIONS_PROP_NAME);
        this.iterations = checkValueRange(builder.iterations, 1, Integer.MAX_VALUE, ITERATIONS_PROP_NAME);
        this.warmupMessageRate = checkValueRange(
            builder.warmupMessageRate, 0, MAX_MESSAGE_RATE, WARMUP_MESSAGE_RATE_PROP_NAME);
        this.messageRate = checkValueRange(builder.messageRate, 1, MAX_MESSAGE_RATE, MESSAGE_RATE_PROP_NAME);
        this.batchSize = checkValueRange(builder.batchSize, 1, Integer.MAX_VALUE, BATCH_SIZE_PROP_NAME);
        this.messageLength =
            checkValueRange(builder.messageLength, MIN_MESSAGE_LENGTH, Integer.MAX_VALUE, MESSAGE_LENGTH_PROP_NAME);
        this.messageTransceiverClass = validateMessageTransceiverClass(builder.messageTransceiverClass);
        this.idleStrategy = requireNonNull(builder.idleStrategy, "'" + IDLE_STRATEGY_PROP_NAME + "' cannot be null");
        this.outputDirectory = validateOutputDirectory(builder.outputDirectory);
        logsDir = resolveLogsDir(outputDirectory);
        this.trackHistory = builder.trackHistory;
        rate = rateAsString();
        outputFileNamePrefix = computeFileNamePrefix(builder.outputFileNamePrefix, builder.systemProperties);
    }

    /**
     * Number of the warmup iterations, where each iteration has a duration of one second. warmup iterations results
     * will be discarded.
     *
     * @return number of the warmup iterations, defaults to {@link #DEFAULT_WARMUP_ITERATIONS}.
     */
    public int warmupIterations()
    {
        return warmupIterations;
    }

    /**
     * Number of the measurement iterations, where each iteration has a duration of one second.
     *
     * @return number of the measurement iterations, defaults to {@link #DEFAULT_ITERATIONS}.
     */
    public int iterations()
    {
        return iterations;
    }

    /**
     * Number of messages to be sent per iteration during the warmup period.
     *
     * @return target message rate per iteration during the warmup period.
     * @implNote Actual number of messages sent can be less than this number if the underlying system is not capable
     * of achieving the target send rate.
     */
    public int warmupMessageRate()
    {
        return warmupMessageRate;
    }

    /**
     * Number of messages to be sent per iteration.
     *
     * @return target message rate per iteration.
     * @implNote Actual number of messages sent can be less than this number if the underlying system is not capable
     * of achieving the target send rate.
     */
    public int messageRate()
    {
        return messageRate;
    }

    /**
     * Size of the batch, i.e. number of messages to be sent in a single burst.
     * <p>
     * For example if the number of messages is {@code 1000} and the batch size is {code 1} then a single message will
     * be sent every millisecond. However if the batch size is {@code 5} then a batch of five messages will be sent
     * every five milliseconds.
     * </p>
     *
     * @return number of messages to be sent in a single burst, defaults to {@link #DEFAULT_BATCH_SIZE}.
     */
    public int batchSize()
    {
        return batchSize;
    }

    /**
     * Length in bytes of a single message.
     *
     * @return length in bytes of a single message, defaults to {@link #MIN_MESSAGE_LENGTH}.
     */
    public int messageLength()
    {
        return messageLength;
    }

    /**
     * {@link MessageTransceiver} class to use for the benchmark.
     *
     * @return {@link MessageTransceiver} class.
     */
    public Class<? extends MessageTransceiver> messageTransceiverClass()
    {
        return messageTransceiverClass;
    }

    /**
     * {@link IdleStrategy} to use when sending and receiving messages.
     *
     * @return sender {@link IdleStrategy}, defaults to {@link NoOpIdleStrategy}.
     */
    public IdleStrategy idleStrategy()
    {
        return idleStrategy;
    }

    /**
     * Output directory used for storing the histogram files.
     *
     * @return output directory.
     */
    public Path outputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Directory to store logs and diagnostics files.
     *
     * @return logs dir location.
     */
    public Path logsDir()
    {
        return logsDir;
    }

    /**
     * Indicate if history should be tracked for the latency test.
     *
     * @return true if history should be tracked.
     */
    public boolean trackHistory()
    {
        return trackHistory;
    }

    /**
     * Output file name prefix used for creating the file name to persist the results histogram.
     *
     * @return output file name prefix.
     */
    public String outputFileNamePrefix()
    {
        return outputFileNamePrefix;
    }

    public String toString()
    {
        return "Configuration{" +
            "\n    warmUpIterations=" + warmupIterations +
            "\n    warmupMessageRate=" + warmupMessageRate +
            "\n    iterations=" + iterations +
            "\n    messageRate=" + rate +
            "\n    batchSize=" + batchSize +
            "\n    messageLength=" + messageLength +
            "\n    messageTransceiverClass=" + messageTransceiverClass.getName() +
            "\n    idleStrategy=" + idleStrategy +
            "\n    outputDirectory=" + outputDirectory +
            "\n    outputFileNamePrefix=" + outputFileNamePrefix +
            "\n}";
    }

    private String rateAsString()
    {
        if (0 == messageRate % 1_000_000)
        {
            return (messageRate / 1_000_000) + "M";
        }
        else if (0 == messageRate % 1000)
        {
            return (messageRate / 1000) + "K";
        }
        else
        {
            return Integer.toString(messageRate);
        }
    }

    private String computeFileNamePrefix(final String outputFileNamePrefix, final Properties systemProperties)
    {
        final String prefix = null != outputFileNamePrefix ? outputFileNamePrefix.trim() : "";
        if (prefix.isEmpty())
        {
            throw new IllegalArgumentException("Output file name prefix cannot be empty!");
        }

        return prefix +
            "_rate=" + rate +
            "_batch=" + batchSize +
            "_length=" + messageLength +
            "_sha=" + computeSha256(systemProperties);
    }

    /**
     * A builder for the {@code Configuration}.
     */
    public static final class Builder
    {
        private int warmupIterations = DEFAULT_WARMUP_ITERATIONS;
        private int iterations = DEFAULT_ITERATIONS;
        private int warmupMessageRate = DEFAULT_WARMUP_MESSAGE_RATE;
        private int messageRate;
        private int batchSize = DEFAULT_BATCH_SIZE;
        private int messageLength = MIN_MESSAGE_LENGTH;
        private Class<? extends MessageTransceiver> messageTransceiverClass;
        private IdleStrategy idleStrategy = NoOpIdleStrategy.INSTANCE;
        private Path outputDirectory = Paths.get("results");
        private Properties systemProperties = System.getProperties();
        private String outputFileNamePrefix;
        private boolean trackHistory = DEFAULT_TRACK_HISTORY;

        /**
         * Set the number of warmup iterations.
         *
         * @param iterations number of warmup iterations.
         * @return this for a fluent API.
         */
        public Builder warmupIterations(final int iterations)
        {
            this.warmupIterations = iterations;
            return this;
        }

        /**
         * Set the number of measurement iterations.
         *
         * @param iterations number of measurement iterations.
         * @return this for a fluent API.
         */
        public Builder iterations(final int iterations)
        {
            this.iterations = iterations;
            return this;
        }

        /**
         * Set the target message rate per iteration during the warmup period.
         *
         * @param warmupMessageRate number of messages to be sent per iteration during the warmup period.
         * @return this for a fluent API.
         */
        public Builder warmupMessageRate(final int warmupMessageRate)
        {
            this.warmupMessageRate = warmupMessageRate;
            return this;
        }

        /**
         * Set the target message rate per iteration.
         *
         * @param messageRate number of messages to be sent per iterations.
         * @return this for a fluent API.
         */
        public Builder messageRate(final int messageRate)
        {
            this.messageRate = messageRate;
            return this;
        }

        /**
         * Set the batch size, i.e. number of messages to be sent at once in a single burst.
         *
         * @param size of a single batch of messages.
         * @return this for a fluent API.
         */
        public Builder batchSize(final int size)
        {
            this.batchSize = size;
            return this;
        }

        /**
         * Set the length of a single message in bytes. Must be at least {@link #MIN_MESSAGE_LENGTH} bytes long, since
         * every message must contain a {@code timestamp} payload.
         *
         * @param length of a single message in bytes.
         * @return this for a fluent API.
         */
        public Builder messageLength(final int length)
        {
            this.messageLength = length;
            return this;
        }

        /**
         * Set the {@link MessageTransceiver} class.
         *
         * @param klass class.
         * @return this for a fluent API.
         */
        public Builder messageTransceiverClass(final Class<? extends MessageTransceiver> klass)
        {
            this.messageTransceiverClass = klass;
            return this;
        }

        /**
         * Set the {@link IdleStrategy} for sending and receiving the messages.
         *
         * @param idleStrategy idle strategy for the sender.
         * @return this for a fluent API.
         */
        public Builder idleStrategy(final IdleStrategy idleStrategy)
        {
            this.idleStrategy = idleStrategy;
            return this;
        }

        /**
         * Set the output directory to store histogram files in.
         *
         * @param outputDirectory output directory.
         * @return this for a fluent API.
         */
        public Builder outputDirectory(final Path outputDirectory)
        {
            this.outputDirectory = outputDirectory;
            return this;
        }

        /**
         * Set the output file name prefix.
         *
         * @param outputFileNamePrefix output directory.
         * @return this for a fluent API.
         */
        public Builder outputFileNamePrefix(final String outputFileNamePrefix)
        {
            this.outputFileNamePrefix = outputFileNamePrefix;
            return this;
        }

        /**
         * Toggle whether the rig should track history.
         *
         * @param trackHistory true to track history false otherwise.
         * @return this for a fluent API.
         */
        public Builder trackHistory(final boolean trackHistory)
        {
            this.trackHistory = trackHistory;
            return this;
        }

        /**
         * Create a new instance of the {@link Configuration} class from this builder.
         *
         * @return a {@link Configuration} instance
         */
        public Configuration build()
        {
            return new Configuration(this);
        }

        Builder systemProperties(final Properties properties)
        {
            systemProperties = properties;
            return this;
        }
    }

    /**
     * Create a {@link Configuration} instance based on the provided system properties.
     *
     * @return a {@link Configuration} instance.
     */
    public static Configuration fromSystemProperties()
    {
        final Builder builder = new Builder();
        if (isPropertyProvided(WARMUP_ITERATIONS_PROP_NAME))
        {
            builder.warmupIterations(intProperty(WARMUP_ITERATIONS_PROP_NAME));
        }

        if (isPropertyProvided(WARMUP_MESSAGE_RATE_PROP_NAME))
        {
            builder.warmupMessageRate(rateProperty(WARMUP_MESSAGE_RATE_PROP_NAME));
        }

        if (isPropertyProvided(ITERATIONS_PROP_NAME))
        {
            builder.iterations(intProperty(ITERATIONS_PROP_NAME));
        }

        if (isPropertyProvided(BATCH_SIZE_PROP_NAME))
        {
            builder.batchSize(intProperty(BATCH_SIZE_PROP_NAME));
        }

        if (isPropertyProvided(MESSAGE_LENGTH_PROP_NAME))
        {
            builder.messageLength(intProperty(MESSAGE_LENGTH_PROP_NAME));
        }

        if (isPropertyProvided(IDLE_STRATEGY_PROP_NAME))
        {
            builder.idleStrategy(resolveIdleStrategy());
        }

        if (isPropertyProvided(OUTPUT_DIRECTORY_PROP_NAME))
        {
            builder.outputDirectory(Paths.get(getProperty(OUTPUT_DIRECTORY_PROP_NAME)));
        }

        if (isPropertyProvided(TRACK_HISTORY_PROP_NAME))
        {
            builder.trackHistory(Boolean.getBoolean(TRACK_HISTORY_PROP_NAME));
        }

        builder
            .messageRate(rateProperty(MESSAGE_RATE_PROP_NAME))
            .messageTransceiverClass(classProperty(MESSAGE_TRANSCEIVER_PROP_NAME, MessageTransceiver.class))
            .outputFileNamePrefix(getPropertyValue(OUTPUT_FILE_NAME_PROP_NAME));

        return builder.build();
    }

    /**
     * Returns directory where TLS certificates are stored.
     *
     * @return directory where TLS certificates are stored.
     */
    public static Path tryResolveCertificatesDirectory()
    {
        final Path userDir = Paths.get(getProperty("user.dir"));
        Path certificatesDir = userDir.resolve("certificates");
        if (exists(certificatesDir))
        {
            return certificatesDir;
        }

        certificatesDir = userDir.getParent().resolve("certificates");
        if (exists(certificatesDir))
        {
            return certificatesDir;
        }

        throw new IllegalStateException("could not find 'certificates' directory under: " + userDir.toAbsolutePath());
    }

    /**
     * Resolve directory to store the log files in.
     *
     * @return directory to store the results and the other outputs.
     */
    public static Path resolveLogsDir()
    {
        final String path = getProperty(OUTPUT_DIRECTORY_PROP_NAME);
        Objects.requireNonNull(path, OUTPUT_DIRECTORY_PROP_NAME);
        final Path directory = Paths.get(path).resolve(LOGS_DIR);
        if (!Files.exists(directory))
        {
            try
            {
                return Files.createDirectories(directory);
            }
            catch (final IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }
        return directory;
    }

    private static int checkValueRange(final int value, final int minValue, final int maxValue, final String propName)
    {
        if (value < minValue)
        {
            throw new IllegalArgumentException(
                "'" + propName + "' cannot be less than " + minValue + ", got: " + value);
        }

        if (value > maxValue)
        {
            throw new IllegalArgumentException(
                "'" + propName + "' cannot be greater than " + maxValue + ", got: " + value);
        }

        return value;
    }

    private static Class<? extends MessageTransceiver> validateMessageTransceiverClass(
        final Class<? extends MessageTransceiver> klass)
    {
        requireNonNull(klass, "MessageTransceiver class cannot be null");
        if (isAbstract(klass.getModifiers()))
        {
            throw new IllegalArgumentException("MessageTransceiver class must be a concrete class");
        }

        try
        {
            final Constructor<? extends MessageTransceiver> constructor = klass.getConstructor(
                NanoClock.class, ValueRecorder.class);
            if (isPublic(constructor.getModifiers()))
            {
                return klass;
            }
        }
        catch (final NoSuchMethodException ignore)
        {
        }

        throw new IllegalArgumentException(
            "MessageTransceiver class must have a public constructor that takes a NanoClock and a ValueRecorder");
    }

    private static boolean isPropertyProvided(final String propName)
    {
        return !isEmpty(getProperty(propName));
    }

    private static int rateProperty(final String propName)
    {
        final String value = getPropertyValue(propName);
        try
        {
            final int lastIndex = value.length() - 1;
            final char lastCharacter = value.charAt(lastIndex);
            if (Character.isDigit(lastCharacter))
            {
                return AsciiEncoding.parseIntAscii(value, 0, value.length());
            }

            final int prefix = AsciiEncoding.parseIntAscii(value, 0, lastIndex);

            switch (lastCharacter)
            {
                case 'K':
                    if (prefix > MAX_K_VALUE)
                    {
                        throw new NumberFormatException(propName + " would overflow an int: " + value);
                    }
                    return prefix * 1000;

                case 'M':
                    if (prefix > MAX_M_VALUE)
                    {
                        throw new NumberFormatException(propName + " would overflow an int: " + value);
                    }
                    return prefix * 1_000_000;

                default:
                    throw new NumberFormatException(
                        propName + ": " + value + " should end with: K or M.");
            }
        }
        catch (final RuntimeException ex)
        {
            throw new IllegalArgumentException("invalid rate specified in '" + propName + "'", ex);
        }
    }

    private static int intProperty(final String propName)
    {
        final String value = getPropertyValue(propName);
        try
        {
            return AsciiEncoding.parseIntAscii(value, 0, value.length());
        }
        catch (final AsciiNumberFormatException ex)
        {
            throw new IllegalArgumentException(
                "non-integer value for property '" + propName + "', cause: " + ex.getMessage());
        }
    }

    private static String getPropertyValue(final String propName)
    {
        final String value = getProperty(propName);
        if (isEmpty(value))
        {
            throw new IllegalArgumentException("property '" + propName + "' is required!");
        }

        return value;
    }

    private static <T> Class<? extends T> classProperty(final String propName, final Class<T> parentClass)
    {
        try
        {
            final Class<?> klass = Class.forName(getPropertyValue(propName));
            return klass.asSubclass(parentClass);
        }
        catch (final ClassNotFoundException | ClassCastException ex)
        {
            throw new IllegalArgumentException(
                "invalid class value for property '" + propName + "', cause: " + ex.getMessage());
        }
    }

    private static IdleStrategy resolveIdleStrategy()
    {
        final Class<? extends IdleStrategy> klass = classProperty(IDLE_STRATEGY_PROP_NAME, IdleStrategy.class);
        try
        {
            return klass.getConstructor().newInstance();
        }
        catch (final InstantiationException | IllegalAccessException | NoSuchMethodException ex)
        {
            throw new IllegalArgumentException(
                "invalid IdleStrategy property '" + IDLE_STRATEGY_PROP_NAME + "', cause: " + ex.getMessage());
        }
        catch (final InvocationTargetException ex)
        {
            throw new IllegalArgumentException(
                "invalid IdleStrategy property '" + IDLE_STRATEGY_PROP_NAME + "', cause: " +
                    ex.getCause().getMessage());
        }
    }

    private static Path validateOutputDirectory(final Path outputDirectory)
    {
        requireNonNull(outputDirectory, "output directory cannot be null");

        if (exists(outputDirectory))
        {
            if (!isDirectory(outputDirectory))
            {
                throw new IllegalArgumentException(
                    "output path is not a directory: " + outputDirectory.toAbsolutePath());
            }

            if (!isWritable(outputDirectory))
            {
                throw new IllegalArgumentException(
                    "output directory is not writeable: " + outputDirectory.toAbsolutePath());
            }
        }
        else
        {
            try
            {
                createDirectories(outputDirectory);
            }
            catch (final IOException e)
            {
                throw new IllegalArgumentException("failed to create output directory: " + outputDirectory, e);
            }
        }

        return outputDirectory.toAbsolutePath();
    }

    private static Path resolveLogsDir(final Path outputDirectory)
    {
        final Path logsDir = outputDirectory.resolve(LOGS_DIR);
        if (!Files.exists(logsDir))
        {
            try
            {
                return Files.createDirectory(logsDir);
            }
            catch (final IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }
        return logsDir;
    }

    static String computeSha256(final Properties properties)
    {
        final TreeMap<String, String> sortedProperties = new TreeMap<>();
        for (final Map.Entry<Object, Object> entry : properties.entrySet())
        {
            final String key = (String)entry.getKey();
            if (!OUTPUT_FILE_NAME_PROP_NAME.equals(key) && !OUTPUT_DIRECTORY_PROP_NAME.equals(key))
            {
                sortedProperties.put(key, (String)entry.getValue());
            }
        }

        return toHex(computeSha256Digest(sortedProperties));
    }

    private static byte[] computeSha256Digest(final TreeMap<String, String> properties)
    {
        synchronized (SHA256)
        {
            SHA256.reset();
            for (final Map.Entry<String, String> entry : properties.entrySet())
            {
                SHA256.update(entry.getKey().getBytes(UTF_8));
                SHA256.update(entry.getValue().getBytes(UTF_8));
            }
            return SHA256.digest();
        }
    }
}
