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
import org.agrona.CloseHelper;
import org.agrona.LangUtil;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.NanoClock;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.agrona.concurrent.SystemNanoClock;

import java.io.PrintStream;
import java.util.Properties;
import java.util.function.BiFunction;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.agrona.PropertyAction.PRESERVE;
import static org.agrona.PropertyAction.REPLACE;
import static uk.co.real_logic.benchmarks.remote.MessageTransceiver.CHECKSUM;
import static uk.co.real_logic.benchmarks.remote.PersistedHistogram.Status.FAIL;
import static uk.co.real_logic.benchmarks.remote.PersistedHistogram.Status.OK;
import static uk.co.real_logic.benchmarks.remote.PersistedHistogram.newPersistedHistogram;
import static uk.co.real_logic.benchmarks.util.PropertiesUtil.loadPropertiesFiles;
import static uk.co.real_logic.benchmarks.util.PropertiesUtil.mergeWithSystemProperties;

/**
 * {@code LoadTestRig} class is the core of the RTT benchmark. It is responsible for running benchmark against provided
 * {@link MessageTransceiver} instance using given {@link Configuration}.
 */
public final class LoadTestRig
{
    private static final long NANOS_PER_SECOND = SECONDS.toNanos(1);
    private static final long RECEIVE_DEADLINE_NS = SECONDS.toNanos(3);
    private final Configuration configuration;
    private final MessageTransceiver messageTransceiver;
    private final PrintStream out;
    private final NanoClock clock;
    private final PersistedHistogram persistedHistogram;
    private final ProgressReporter progressReporter;

    public LoadTestRig(final Configuration configuration)
    {
        this(configuration, SystemNanoClock.INSTANCE, newPersistedHistogram(configuration), System.out);
    }

    public LoadTestRig(
        final Configuration configuration,
        final NanoClock nanoClock,
        final PersistedHistogram persistedHistogram,
        final PrintStream out)
    {
        this(
            configuration,
            nanoClock,
            persistedHistogram,
            (nc, ph) -> createTransceiver(configuration, nc, ph),
            out);
    }

    public LoadTestRig(
        final Configuration configuration,
        final NanoClock nanoClock,
        final PersistedHistogram persistedHistogram,
        final BiFunction<NanoClock, ValueRecorder, MessageTransceiver> transceiverFactory,
        final PrintStream out)
    {
        this(
            configuration,
            transceiverFactory.apply(nanoClock, persistedHistogram.valueRecorder()),
            out,
            nanoClock,
            persistedHistogram,
            new AsyncProgressReporter(out, new OneToOneConcurrentArrayQueue<>(16)));
    }

    LoadTestRig(
        final Configuration configuration,
        final MessageTransceiver messageTransceiver,
        final PrintStream out,
        final NanoClock clock,
        final PersistedHistogram persistedHistogram,
        final ProgressReporter progressReporter)
    {
        this.configuration = requireNonNull(configuration);
        this.messageTransceiver = requireNonNull(messageTransceiver);
        this.out = requireNonNull(out);
        this.clock = requireNonNull(clock);
        this.persistedHistogram = requireNonNull(persistedHistogram);
        this.progressReporter = progressReporter;
    }

    /**
     * Run the benchmark and print histogram of the RTT values at the end.
     *
     * @throws Exception in case of any error from the {@link MessageTransceiver}
     */
    public void run() throws Exception
    {
        out.printf("%nStarting latency benchmark using the following configuration:%n%s%n", configuration);

        try
        {
            messageTransceiver.init(configuration);
            if (configuration.warmupIterations() > 0)
            {
                out.printf("%nRunning warmup for %,d iterations of %,d messages each, with %,d bytes payload and a" +
                    " burst size of %,d...%n",
                    configuration.warmupIterations(),
                    configuration.warmupMessageRate(),
                    configuration.messageLength(),
                    configuration.batchSize());
                send(configuration.warmupIterations(), configuration.warmupMessageRate());

                messageTransceiver.reset();
                persistedHistogram.reset();
                progressReporter.reset();
            }

            out.printf("%nRunning measurement for %,d iterations of %,d messages each, with %,d bytes payload and a" +
                " burst size of %,d...%n",
                configuration.iterations(),
                configuration.messageRate(),
                configuration.messageLength(),
                configuration.batchSize());
            final SendResult result = send(configuration.iterations(), configuration.messageRate());
            progressReporter.reset();

            out.printf("%nHistogram of RTT latencies in microseconds.%n");
            final PersistedHistogram histogram = persistedHistogram;
            histogram.outputPercentileDistribution(out, 1000.0);

            final long expectedTotalNumberOfMessages = configuration.iterations() * (long)configuration.messageRate();
            warnIfTargetRateNotAchieved(result, expectedTotalNumberOfMessages);

            final PersistedHistogram.Status status = result.status(expectedTotalNumberOfMessages);
            histogram.saveToFile(
                configuration.outputDirectory(),
                configuration.outputFileNamePrefix(),
                status);
            if (configuration.trackHistory())
            {
                histogram.saveHistoryToCsvFile(
                    configuration.outputDirectory(),
                    configuration.outputFileNamePrefix(),
                    status,
                    50.0, 99.0, 99.9, 99.99, 100.0);
            }
        }
        finally
        {
            messageTransceiver.destroy();
            CloseHelper.close(persistedHistogram);
        }
    }

    @SuppressWarnings("MethodLength")
    SendResult send(final int iterations, final int numberOfMessages)
    {
        final MessageTransceiver messageTransceiver = this.messageTransceiver;
        final NanoClock clock = this.clock;
        final int burstSize = configuration.batchSize();
        final int messageSize = configuration.messageLength();
        final IdleStrategy idleStrategy = configuration.idleStrategy();
        // The `sendIntervalNs` might be off if the division is not exact in which case more messages will be sent per
        // second than specified via `numberOfMessages`. However, this guarantees that the duration of the send
        // operation is bound by the number of iterations.
        final long sendIntervalNs = NANOS_PER_SECOND * burstSize / numberOfMessages;
        final long totalNumberOfMessages = (long)iterations * numberOfMessages;
        final long startTimeNs = clock.nanoTime();
        final long stopTimeNs = startTimeNs + (iterations * NANOS_PER_SECOND);

        long sentMessages = 0;
        long nowNs = startTimeNs, timestampNs = startTimeNs;
        long nextReportTimeNs = startTimeNs + NANOS_PER_SECOND;

        int batchSize = (int)min(totalNumberOfMessages, burstSize);
        while (sentMessages < totalNumberOfMessages)
        {
            final int sent = messageTransceiver.send(batchSize, messageSize, timestampNs, CHECKSUM);
            sentMessages += sent;

            if (totalNumberOfMessages == sentMessages)
            {
                progressReporter.reportProgress(startTimeNs, nowNs, sentMessages, iterations);
                break;
            }

            nowNs = clock.nanoTime();
            if (sent == batchSize)
            {
                batchSize = (int)min(totalNumberOfMessages - sentMessages, burstSize);
                timestampNs += sendIntervalNs;
                long receivedMessageCount = 0;
                while (nowNs < timestampNs && nowNs < stopTimeNs)
                {
                    if (nowNs >= nextReportTimeNs)
                    {
                        progressReporter.reportProgress(startTimeNs, nowNs, sentMessages, iterations);
                        nextReportTimeNs += NANOS_PER_SECOND;
                    }

                    if (receivedMessageCount < sentMessages)
                    {
                        messageTransceiver.receive();
                        final long newReceivedMessageCount = messageTransceiver.receivedMessages();
                        if (newReceivedMessageCount == receivedMessageCount)
                        {
                            idleStrategy.idle();
                        }
                        else
                        {
                            receivedMessageCount = newReceivedMessageCount;
                            idleStrategy.reset();
                        }
                    }
                    else
                    {
                        idleStrategy.idle();
                    }

                    nowNs = clock.nanoTime();
                }
            }
            else
            {
                batchSize -= sent;
                messageTransceiver.receive();
            }

            if (nowNs >= stopTimeNs)
            {
                break;
            }

            if (nowNs >= nextReportTimeNs)
            {
                progressReporter.reportProgress(startTimeNs, nowNs, sentMessages, iterations);
                nextReportTimeNs += NANOS_PER_SECOND;
            }
        }

        idleStrategy.reset();
        long receivedMessageCount = messageTransceiver.receivedMessages();
        final long deadline = clock.nanoTime() + RECEIVE_DEADLINE_NS;
        while (receivedMessageCount < sentMessages)
        {
            messageTransceiver.receive();
            final long newReceivedMessageCount = messageTransceiver.receivedMessages();
            if (newReceivedMessageCount == receivedMessageCount)
            {
                idleStrategy.idle();
                if (clock.nanoTime() >= deadline)
                {
                    break;
                }
            }
            else
            {
                receivedMessageCount = newReceivedMessageCount;
                idleStrategy.reset();
            }
        }

        return new SendResult(sentMessages, receivedMessageCount);
    }

    private void warnIfTargetRateNotAchieved(final SendResult result, final long expectedTotalNumberOfMessages)
    {
        if (expectedTotalNumberOfMessages != result.sentMessages)
        {
            out.printf(
                "%n*** WARNING: Target message rate not achieved: expected to send %,d messages in " +
                "total but managed to send only %,d messages (loss %.4f%%)!%n",
                expectedTotalNumberOfMessages,
                result.sentMessages,
                100.0 - (100.0 * result.sentMessages / expectedTotalNumberOfMessages));
        }

        if (result.sentMessages != result.receivedMessages)
        {
            out.printf(
                "%n*** WARNING: Not all messages were received after %ds deadline: expected %,d vs received " +
                "%,d (loss %.4f%%)!%n",
                NANOSECONDS.toSeconds(RECEIVE_DEADLINE_NS),
                result.sentMessages,
                result.receivedMessages,
                100.0 - (100.0 * result.receivedMessages / result.sentMessages));
        }
    }

    private static MessageTransceiver createTransceiver(
        final Configuration configuration,
        final NanoClock nanoClock,
        final ValueRecorder valueRecorder)
    {
        try
        {
            return configuration
                .messageTransceiverClass()
                .getConstructor(NanoClock.class, ValueRecorder.class)
                .newInstance(nanoClock, valueRecorder);
        }
        catch (final ReflectiveOperationException ex)
        {
            LangUtil.rethrowUnchecked(ex);
            throw new Error();
        }
    }

    public static void main(final String[] args) throws Exception
    {
        Thread.currentThread().setName("load-test-rig");
        mergeWithSystemProperties(PRESERVE, loadPropertiesFiles(new Properties(), REPLACE, args));

        final Configuration configuration = Configuration.fromSystemProperties();

        new LoadTestRig(configuration).run();
    }

    static final class SendResult
    {
        final long sentMessages;
        final long receivedMessages;

        SendResult(final long sentMessages, final long receivedMessages)
        {
            this.sentMessages = sentMessages;
            this.receivedMessages = receivedMessages;
        }

        PersistedHistogram.Status status(final long expectedNumberOfMessages)
        {
            return expectedNumberOfMessages == sentMessages && expectedNumberOfMessages == receivedMessages ? OK : FAIL;
        }
    }
}
