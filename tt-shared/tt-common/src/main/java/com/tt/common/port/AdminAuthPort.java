package com.tt.common.port;

/** 管理端鉴权端口，由 application 层实现，infrastructure 层 JWT 过滤器依赖此接口。 */
public interface AdminAuthPort {

  boolean hasAdminRole(Long userId);
}
