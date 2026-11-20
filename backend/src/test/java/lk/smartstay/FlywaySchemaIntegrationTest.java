package lk.smartstay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies actual PostgreSQL migrations and seed data, not just Java compilation.
 * Local tests require the Compose database and matching DB_PASSWORD.
 */
@SpringBootTest
class FlywaySchemaIntegrationTest {
    @Autowired JdbcTemplate jdbc;

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
    void overlappingConfirmedReservationsAreBlockedAtDatabaseLevel() {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'ex_room_active_stay'",
            Integer.class);
        assertEquals(1, count);
    }

    @Test
    void invoiceSnapshotsAndRefundColumnsExist() {
        Integer snapshots = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.columns
             WHERE table_name = 'invoices' AND column_name = 'daily_rate_snapshot'
            """, Integer.class);
        Integer refunds = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.columns
             WHERE table_name = 'reservations' AND column_name = 'cancellation_refund_due'
            """, Integer.class);
        assertEquals(1, snapshots);
        assertEquals(1, refunds);
    }
}
