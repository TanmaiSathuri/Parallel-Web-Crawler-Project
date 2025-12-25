package com.udacity.webcrawler.json;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class CrawlResultWriter {

  private final CrawlResult result;

  public CrawlResultWriter(CrawlResult result) {
    this.result = result;
  }

  public void write(Path path) throws IOException {
    try (Writer writer = Files.newBufferedWriter(
            path,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
    )) {
      write(writer);
    }
  }

  public void write(Writer writer) throws IOException {
    Moshi moshi = new Moshi.Builder().build();

    Type type = Types.newParameterizedType(
            Map.class,
            String.class,
            Object.class
    );

    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);

    Map<String, Object> jsonMap = new java.util.LinkedHashMap<>();
    jsonMap.put("wordCounts", result.getWordCounts());
    jsonMap.put("urlsVisited", result.getUrlsVisited());


    writer.write(adapter.indent("  ").toJson(jsonMap));
  }
}



