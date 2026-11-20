package lk.smartstay.identity.persistence;

import java.util.List;
import lk.smartstay.identity.api.UiManifest.NavigationItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NavigationRepository {
    private final JdbcTemplate jdbc;

    public NavigationRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<NavigationItem> active() {
        return jdbc.query("""
            SELECT id,label,route_key,icon_key,sort_order,required_permission
            FROM navigation_items WHERE enabled = TRUE ORDER BY sort_order,id
            """, (rs, row) -> new NavigationItem(rs.getString("id"),
            rs.getString("label"), rs.getString("route_key"), rs.getString("icon_key"),
            rs.getInt("sort_order"), rs.getString("required_permission")));
    }

    public List<NavigationItem> all() {
        return jdbc.query("""
            SELECT id,label,route_key,icon_key,sort_order,required_permission
            FROM navigation_items ORDER BY sort_order,id
            """, (rs, row) -> new NavigationItem(rs.getString("id"),
            rs.getString("label"), rs.getString("route_key"), rs.getString("icon_key"),
            rs.getInt("sort_order"), rs.getString("required_permission")));
    }

    public int update(String key, String label, int order, boolean enabled) {
        return jdbc.update("""
            UPDATE navigation_items SET label = ?, sort_order = ?, enabled = ?
            WHERE id = ?
            """, label, order, enabled, key);
    }
}
