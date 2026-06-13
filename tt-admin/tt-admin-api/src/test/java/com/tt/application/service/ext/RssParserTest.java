package com.tt.application.service.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RssParserTest {

  private final RssParser parser = new RssParser();

  @Test
  void parse_extractsItemsFromRss20() {
    String xml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0"><channel>
          <item>
            <title>  News One  </title>
            <link>https://example.com/a</link>
            <description>Summary A</description>
          </item>
          <item>
            <title>News Two</title>
            <link>https://example.com/b</link>
          </item>
        </channel></rss>
        """;
    var items = parser.parse(xml, "https://example.com");
    assertEquals(2, items.size());
    assertEquals("News One", items.get(0).get("title"));
    assertEquals("https://example.com/a", items.get(0).get("external_url"));
    assertEquals("Summary A", items.get(0).get("summary"));
  }

  @Test
  void parse_returnsEmptyForBlankInput() {
    assertTrue(parser.parse("  ", "https://example.com").isEmpty());
  }
}
