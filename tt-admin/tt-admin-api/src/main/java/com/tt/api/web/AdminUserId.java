package com.tt.api.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AdminUserId {

  private AdminUserId() {}

  public static long get() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || a.getPrincipal() == null) {
      throw new IllegalStateException("未登录");
    }
    Object p = a.getPrincipal();
    if (p instanceof Long id) {
      return id;
    }
    return Long.parseLong(String.valueOf(p));
  }
}
