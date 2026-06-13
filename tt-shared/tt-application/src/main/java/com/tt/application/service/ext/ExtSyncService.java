package com.tt.application.service.ext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class ExtSyncService {

  private static final String USER_AGENT = "TT-ExtSync/1.0 (+https://github.com/tt-event)";

  private final JdbcTemplate jdbc;
  private final HtmlV1Parser htmlV1Parser;
  private final RssParser rssParser;
  private final RestTemplate restTemplate;

  public ExtSyncService(JdbcTemplate jdbc, HtmlV1Parser htmlV1Parser, RssParser rssParser) {
    this.jdbc = jdbc;
    this.htmlV1Parser = htmlV1Parser;
    this.rssParser = rssParser;
    this.restTemplate = createRestTemplate();
  }

  @Transactional
  public Map<String, Object> runSyncSource(long sourceId) {
    List<Map<String, Object>> src =
        jdbc.queryForList("SELECT * FROM ext_data_source WHERE id = ?", sourceId);
    if (src.isEmpty()) throw new IllegalArgumentException("数据源不存在");
    Map<String, Object> row = src.get(0);
    if (row.get("enabled") != null && ((Number) row.get("enabled")).intValue() == 0) {
      throw new IllegalStateException("数据源已禁用");
    }
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(
        conn -> {
          PreparedStatement ps =
              conn.prepareStatement(
                  "INSERT INTO ext_sync_log (source_id, started_at, status, fetched_count) VALUES (?, CURRENT_TIMESTAMP(3), 'running', 0)",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setLong(1, sourceId);
          return ps;
        },
        kh);
    long lid = kh.getKey().longValue();
    try {
      String baseUrl = (String) row.get("base_url");
      String body = fetchBody(baseUrl);
      String parser = row.get("parser_type") == null ? "html_v1" : String.valueOf(row.get("parser_type"));
      List<Map<String, Object>> items = parseItems(parser, body, baseUrl);
      int fetched = 0;
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      for (Map<String, Object> it : items) {
        String url = String.valueOf(it.get("external_url"));
        if (url.length() > 2048) url = url.substring(0, 2048);
        String title = String.valueOf(it.get("title"));
        String hash =
            bytesToHex(md.digest((url + title).getBytes(StandardCharsets.UTF_8))).substring(0, 32);
        Integer exists =
            jdbc.queryForObject(
                "SELECT COUNT(1) FROM ext_feed_item WHERE source_id = ? AND external_url = ?",
                Integer.class,
                sourceId,
                url);
        if (exists != null && exists == 0) {
          jdbc.update(
              "INSERT INTO ext_feed_item (source_id, external_url, title, summary, published_at_ext, raw_hash) VALUES (?,?,?,?,?,?)",
              sourceId,
              url,
              title,
              it.get("summary"),
              null,
              hash);
          fetched++;
        }
      }
      jdbc.update(
          "UPDATE ext_sync_log SET ended_at = CURRENT_TIMESTAMP(3), status = 'success', fetched_count = ?, error_message = NULL WHERE id = ?",
          fetched,
          lid);
      return Map.of("logId", lid, "fetched", fetched, "sourceId", sourceId);
    } catch (Exception e) {
      String msg = e.getMessage() != null ? e.getMessage() : String.valueOf(e);
      if (msg.length() > 4000) msg = msg.substring(0, 4000);
      jdbc.update(
          "UPDATE ext_sync_log SET ended_at = CURRENT_TIMESTAMP(3), status = 'error', error_message = ? WHERE id = ?",
          msg,
          lid);
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private String fetchBody(String baseUrl) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
    headers.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    ResponseEntity<String> resp =
        restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("HTTP " + resp.getStatusCode().value());
    }
    return resp.getBody();
  }

  private List<Map<String, Object>> parseItems(String parser, String body, String baseUrl) {
    if ("rss".equalsIgnoreCase(parser) || "rss_v1".equalsIgnoreCase(parser)) {
      return rssParser.parse(body, baseUrl);
    }
    return htmlV1Parser.parse(body, baseUrl);
  }

  private static RestTemplate createRestTemplate() {
    RestTemplate rt = new RestTemplate();
    rt.setRequestFactory(
        new org.springframework.http.client.SimpleClientHttpRequestFactory() {
          {
            setConnectTimeout(10_000);
            setReadTimeout(30_000);
          }
        });
    return rt;
  }

  private static String bytesToHex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) sb.append(String.format("%02x", x));
    return sb.toString();
  }
}
