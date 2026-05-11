package com.LlaveMaestra.ExtractorInfoDB.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DataSourceInterceptor dataSourceInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/html/index.html");
        registry.addViewController("/manual").setViewName("forward:/html/manual.html");
        registry.addViewController("/ayuda").setViewName("forward:/html/manual.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataSourceInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/connection/**");
    }
}
