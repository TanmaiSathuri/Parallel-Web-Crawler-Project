package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class ParallelWebCrawler implements WebCrawler {

  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final PageParserFactory parserFactory;
  private final ForkJoinPool pool;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          PageParserFactory parserFactory,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @TargetParallelism int threadCount) {

    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;

    int actualThreads = Math.min(threadCount, getMaxParallelism());
    this.pool = new ForkJoinPool(actualThreads);
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);

    // Thread-safe shared storage
    Set<String> visited = ConcurrentHashMap.newKeySet();
    Map<String, Integer> wordCounts = new ConcurrentHashMap<>();

    // Create independent tasks for each seed URL
    List<RecursiveAction> tasks = startingUrls.stream()
            .map(url -> new CrawlJob(url, maxDepth, deadline, visited, wordCounts))
            .collect(Collectors.toList());

    // Execute tasks
    for (RecursiveAction task : tasks) {
      pool.invoke(task);
    }

    // Produce sorted results
    Map<String, Integer> sorted = WordCounts.sort(wordCounts, popularWordCount);

    return new CrawlResult.Builder()
            .setWordCounts(sorted)
            .setUrlsVisited(visited.size())
            .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

  /**
   * Recursive crawl task designed for ForkJoinPool.
   */
  private class CrawlJob extends RecursiveAction {

    private final String url;
    private final int depth;
    private final Instant deadline;
    private final Set<String> visitedUrls;
    private final Map<String, Integer> aggregateCounts;

    CrawlJob(String url,
             int depth,
             Instant deadline,
             Set<String> visitedUrls,
             Map<String, Integer> aggregateCounts) {
      this.url = url;
      this.depth = depth;
      this.deadline = deadline;
      this.visitedUrls = visitedUrls;
      this.aggregateCounts = aggregateCounts;
    }

    @Override
    protected void compute() {
      // Stop for timeout or depth limit
      if (depth == 0 || clock.instant().isAfter(deadline)) {
        return;
      }

      // Skip ignored URLs
      for (Pattern p : ignoredUrls) {
        if (p.matcher(url).matches()) {
          return;
        }
      }

      // If already visited, skip
      if (!visitedUrls.add(url)) {
        return;
      }

      // Parse page
      PageParser.Result parsed = parserFactory.get(url).parse();

      // Merge word counts thread-safely
      parsed.getWordCounts().forEach(
              (word, count) -> aggregateCounts.merge(word, count, Integer::sum)
      );

      // Create subtasks for links
      List<CrawlJob> subtasks = parsed.getLinks().stream()
              .map(link -> new CrawlJob(
                      link,
                      depth - 1,
                      deadline,
                      visitedUrls,
                      aggregateCounts))
              .collect(Collectors.toList());

      // Run subtasks in parallel
      invokeAll(subtasks);
    }
  }
}
