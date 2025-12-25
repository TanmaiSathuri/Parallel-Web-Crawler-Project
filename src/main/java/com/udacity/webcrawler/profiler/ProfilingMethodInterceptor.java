package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Object delegate;
  private final ProfilingState state;
  private final Clock clock;

  ProfilingMethodInterceptor(Object delegate, ProfilingState state, Clock clock) {
    this.delegate = delegate;
    this.state = state;
    this.clock = clock;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    boolean profiled = method.getAnnotation(Profiled.class) != null;
    Instant start = null;

    if (profiled) {
      start = clock.instant();
    }

    try {
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } finally {
      if (profiled && start != null) {
        Duration elapsed = Duration.between(start, clock.instant());
        state.record(delegate.getClass(), method, elapsed);
      }
    }
  }
}
