package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A {@link WebCrawler} that downloads and processes one page at a time.
 */
final class SequentialWebCrawler implements WebCrawler {

  private final Clock clock;
  private final PageParserFactory parserFactory;
  private final Duration timeout;
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;

  @Inject
  SequentialWebCrawler(
          Clock clock,
          PageParserFactory parserFactory,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new HashMap<>();
    Set<String> visitedUrls = new HashSet<>();
    for (String url : startingUrls) {
      crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
    }

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
              .setWordCounts(counts)
              .setUrlsVisited(visitedUrls.size())
              .build();
    }

    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build();
  }

  private void crawlInternal(
          String url,
          Instant deadline,
          int depth,
          Map<String, Integer> counts,
          Set<String> visitedUrls) {

    // Stop if depth is zero or timeout reached
    if (depth == 0 || clock.instant().isAfter(deadline)) {
      return;
    }

    // 1. Ignore URLs based on pattern BEFORE anything else
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) {
        return;
      }
    }

    // 2. If already visited, skip
    if (!visitedUrls.add(url)) {
      return;
    }

    // 3. Parse the page
    PageParser.Result result = parserFactory.get(url).parse();

    // 4. Count words
    for (Map.Entry<String, Integer> entry : result.getWordCounts().entrySet()) {
      counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
    }

    // 5. Follow links
    for (String link : result.getLinks()) {
      crawlInternal(link, deadline, depth - 1, counts, visitedUrls);
    }
  }
}
