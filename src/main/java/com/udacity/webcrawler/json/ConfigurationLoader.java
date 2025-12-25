package com.udacity.webcrawler.json;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okio.Buffer;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigurationLoader {

  private final Path configPath;

  public ConfigurationLoader(Path configPath) {
    this.configPath = configPath;
  }

  /** Load config from file */
  public CrawlerConfiguration load() throws IOException {
    try (Reader reader = Files.newBufferedReader(configPath)) {
      return read(reader);  // read() must NOT throw IOException
    }
  }

  /** Udacity requires: NO checked exceptions */
  public static CrawlerConfiguration read(Reader reader) {
    try {
      // Convert Reader -> Buffer
      Buffer buffer = new Buffer();
      char[] chars = new char[1024];
      int read;
      while ((read = reader.read(chars)) != -1) {
        buffer.writeUtf8(new String(chars, 0, read));
      }

      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<CrawlerConfiguration.Builder> adapter =
              moshi.adapter(CrawlerConfiguration.Builder.class);

      CrawlerConfiguration.Builder builder = adapter.fromJson(buffer);

      if (builder == null) {
        throw new RuntimeException("Failed to parse configuration JSON");
      }

      return builder.build();

    } catch (IOException e) {
      throw new RuntimeException(e);   // REQUIRED by Udacity tests
    }
  }
}

