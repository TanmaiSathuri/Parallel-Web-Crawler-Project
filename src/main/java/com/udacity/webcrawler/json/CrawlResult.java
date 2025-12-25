package com.udacity.webcrawler.json;

import com.squareup.moshi.Json;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data class representing the final result of a web crawl.
 */
public final class CrawlResult {

  @Json(name = "wordCounts")
  private final Map<String, Integer> wordCounts;

  @Json(name = "urlsVisited")
  private final int urlsVisited;

  /**
   * Constructs a {@link CrawlResult} with the given word counts and visited URL count.
   */
  private CrawlResult(Map<String, Integer> wordCounts, int urlsVisited) {
    this.wordCounts = wordCounts;
    this.urlsVisited = urlsVisited;
  }

  public Map<String, Integer> getWordCounts() {
    return wordCounts;
  }

  public int getUrlsVisited() {
    return urlsVisited;
  }

  /**
   * A package-private builder class for constructing web crawl {@link CrawlResult}s.
   */
  public static final class Builder {
    private Map<String, Integer> wordFrequencies = new HashMap<>();
    private int pageCount;

    public Builder setWordCounts(Map<String, Integer> wordCounts) {
      this.wordFrequencies = Objects.requireNonNull(wordCounts);
      return this;
    }

    public Builder setUrlsVisited(int pageCount) {
      this.pageCount = pageCount;
      return this;
    }

    public CrawlResult build() {
      return new CrawlResult(Collections.unmodifiableMap(wordFrequencies), pageCount);
    }
  }
}
