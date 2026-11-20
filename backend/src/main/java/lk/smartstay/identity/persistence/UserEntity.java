package lk.smartstay.identity.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class UserEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "guest_id", unique = true)
    private UUID guestId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "token_version", nullable = false)
    private long tokenVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserEntity() { }

    public UserEntity(UUID id, String email, String passwordHash, String displayName,
                      UUID guestId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.displayName = Objects.requireNonNull(displayName);
        this.guestId = guestId;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.active = true;
        this.tokenVersion = 0;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public UUID getGuestId() { return guestId; }
    public boolean isActive() { return active; }
    public long getTokenVersion() { return tokenVersion; }
    public void invalidateTokens() { tokenVersion++; }
    public void disable() { active = false; invalidateTokens(); }
}
