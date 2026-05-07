package com.LlaveMaestra.ExtractorInfoDB.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que establece la clave del DataSource dinámico basándose en el ID de sesión.
 */
@Component
@Slf4j
public class DataSourceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.trace("Setting DataSource key for session: {}", session.getId());
            DynamicDataSourceContextHolder.setDataSourceKey(session.getId());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DynamicDataSourceContextHolder.clearDataSourceKey();
    }
}
