package lk.smartstay.identity.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Reads current grants on every authorized request: JWT claims never grant permissions. */
@Repository
public class AccessRepository {
    private final JdbcTemplate jdbc;

    public AccessRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<String> permissions(UUID userId) {
        return jdbc.queryForList("""
            SELECT DISTINCT rp.permission_code
            FROM user_roles ur JOIN role_permissions rp ON rp.role_code = ur.role_code
            WHERE ur.user_id = ?
            ORDER BY rp.permission_code
            """, String.class, userId);
    }

    public List<String> roles(UUID userId) {
        return jdbc.queryForList("""
            SELECT role_code FROM user_roles WHERE user_id = ? ORDER BY role_code
            """, String.class, userId);
    }

    public void assignRole(UUID userId, String role) {
        jdbc.update("INSERT INTO user_roles(user_id, role_code) VALUES (?, ?)", userId, role);
    }

    public List<String> rolePermissions(String role) {
        return jdbc.queryForList("""
            SELECT permission_code FROM role_permissions WHERE role_code = ?
            ORDER BY permission_code
            """, String.class, role);
    }

    public List<String> permissionCatalog() {
        return jdbc.queryForList("SELECT code FROM permissions ORDER BY code", String.class);
    }

    public boolean roleExists(String code) {
        Boolean exists = jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM roles WHERE code = ?)", Boolean.class, code);
        return Boolean.TRUE.equals(exists);
    }

    public void replaceRolePermissions(String role, List<String> grants) {
        jdbc.update("DELETE FROM role_permissions WHERE role_code = ?", role);
        for (String grant : grants) {
            jdbc.update("INSERT INTO role_permissions(role_code, permission_code) VALUES (?, ?)",
                role, grant);
        }
    }
}
