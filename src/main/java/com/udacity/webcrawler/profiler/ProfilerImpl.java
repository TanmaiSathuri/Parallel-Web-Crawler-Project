package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    long profiledMethodCount =
            java.util.Arrays.stream(klass.getMethods())
                    .filter(m -> m.getAnnotation(Profiled.class) != null)
                    .count();

    if (profiledMethodCount == 0) {
      throw new IllegalArgumentException("Class has no @Profiled methods");
    }

    return klass.cast(
            java.lang.reflect.Proxy.newProxyInstance(
                    klass.getClassLoader(),
                    new Class<?>[]{klass},
                    new ProfilingMethodInterceptor(delegate, state, clock)
            )
    );
  }

  @Override
  public void writeData(Path path) {
    try (Writer writer = java.nio.file.Files.newBufferedWriter(
            path,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND
    )) {
      writeData(writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
