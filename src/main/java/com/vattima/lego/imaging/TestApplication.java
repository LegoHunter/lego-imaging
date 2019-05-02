package com.vattima.lego.imaging;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.ibatis.configuration.DataSourceProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"net.bricklink", "com.bricklink", "com.vattima"})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

//    @Component
    @RequiredArgsConstructor
    @Slf4j
    static class PropertiesTest implements ApplicationRunner {
        private final DataSourceProperties properties;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("properties [{}]", properties);
        }
    }
}
