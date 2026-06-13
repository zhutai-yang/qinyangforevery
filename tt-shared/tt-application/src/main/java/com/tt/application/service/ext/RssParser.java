package com.tt.application.service.ext;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class RssParser {

  public List<Map<String, Object>> parse(String xml, String baseUrl) {
    if (xml == null || xml.isBlank()) {
      return List.of();
    }
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      factory.setExpandEntityReferences(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xml)));
      NodeList items = doc.getElementsByTagName("item");
      List<Map<String, Object>> out = new ArrayList<>();
      for (int i = 0; i < items.getLength() && out.size() < 30; i++) {
        Node node = items.item(i);
        if (!(node instanceof Element item)) continue;
        String link = text(item, "link");
        String title = text(item, "title").replaceAll("\\s+", " ").trim();
        if (link.isEmpty() || title.length() < 2) continue;
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("external_url", link.length() > 2048 ? link.substring(0, 2048) : link);
        row.put("title", title.length() > 500 ? title.substring(0, 500) : title);
        row.put("summary", text(item, "description"));
        out.add(row);
      }
      return out;
    } catch (Exception e) {
      throw new IllegalArgumentException("RSS parse failed: " + e.getMessage(), e);
    }
  }

  private static String text(Element parent, String tag) {
    NodeList nodes = parent.getElementsByTagName(tag);
    if (nodes.getLength() == 0) return "";
    Node n = nodes.item(0);
    return n == null || n.getTextContent() == null ? "" : n.getTextContent().trim();
  }
}
