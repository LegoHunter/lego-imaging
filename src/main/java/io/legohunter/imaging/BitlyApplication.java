package io.legohunter.imaging;

import io.legohunter.imaging.model.bitly.Bitlink;
import io.legohunter.imaging.model.bitly.ShortenRequest;
import io.legohunter.imaging.service.bitly.BitlinksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties
@SpringBootApplication
public class BitlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(BitlyApplication.class, args);
    }

    //@Component
    @RequiredArgsConstructor
    private class BitlyRunner implements ApplicationRunner {
        private final BitlinksService bitlinksService;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("BitlyApplication");
            ShortenRequest request = new ShortenRequest();
            request.setLongUrl("https://vincentspizzeriaandgrill.foodtecsolutions.com/ordering/home");
            Bitlink response = bitlinksService.shorten(request);
            log.info("bitly shorten response [{}]", response);

        }
    }
}
