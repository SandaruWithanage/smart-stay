package lk.smartstay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the actual PostgreSQL migration in CI. Run docker compose up -d db
 * and export DB_URL, DB_USERNAME, DB_PASSWORD for local Maven tests.
 */
@SpringBootTest
class FlywaySchemaIntegrationTest {
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void roomPackagesAreSeededFromTheCoursework() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM room_packages", Integer.class);
        assertEquals(4, count);

        Integer rate = jdbc.queryForObject(
            "SELECT daily_rate::INTEGER FROM room_packages WHERE room_type = 'FAMILY_SUITE'",
            Integer.class);
        assertEquals(18000, rate);
    }

    @Test
    void reservationDateOverlapConstraintExists() {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'ex_room_active_stay'",
            Integer.class);
        assertEquals(1, count);
    }
}
