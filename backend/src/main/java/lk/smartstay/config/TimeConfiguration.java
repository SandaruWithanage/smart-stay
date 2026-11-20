package lk.smartstay.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfiguration {
    public static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Colombo");

    @Bean
    Clock hotelClock() { return Clock.system(HOTEL_ZONE); }
}
