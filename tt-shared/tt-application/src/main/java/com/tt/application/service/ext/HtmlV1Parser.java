package com.tt.application.service.ext;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class HtmlV1Parser {

  public List<Map<String, Object>> parse(String html, String baseUrl) {
    Document doc = Jsoup.parse(html);
    String base = baseUrl == null ? "" : baseUrl.replaceAll("/$", "");
    List<Map<String, Object>> out = new ArrayList<>();
    for (Element a : doc.select("a[href]")) {
      if (out.size() >= 30) break;
      String href = a.attr("href");
      String title = a.text().replaceAll("\\s+", " ").trim();
      if (href.isEmpty() || title.length() < 4) continue;
      String url;
      if (href.startsWith("/")) url = base + href;
      else if (href.matches("(?i)https?://.*")) url = href;
      else continue;
      try {
        URI.create(url);
      } catch (Exception e) {
        continue;
      }
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("external_url", url);
      row.put("title", title.length() > 500 ? title.substring(0, 500) : title);
      row.put("summary", null);
      out.add(row);
    }
    return out;
  }
}
