package lk.smartstay.guest.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

/** Database identity and contact record. Does not confer an authenticated login. */
@Entity
@Table(name = "guests")
public class GuestEntity {
    @Id
    private UUID id;

    @Column(name = "document_number", nullable = false, unique = true, length = 64)
    private String documentNumber;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "contact_number", nullable = false, length = 32)
    private String contactNumber;

    @Column(nullable = false, length = 254)
    private String email;

    protected GuestEntity() { }

    public GuestEntity(UUID id, String documentNumber, String fullName, String contactNumber, String email) {
        this.id = Objects.requireNonNull(id, "id");
        this.documentNumber = Objects.requireNonNull(documentNumber, "documentNumber");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.contactNumber = Objects.requireNonNull(contactNumber, "contactNumber");
        this.email = Objects.requireNonNull(email, "email");
    }

    public UUID getId() { return id; }
    public String getDocumentNumber() { return documentNumber; }
    public String getFullName() { return fullName; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }
}
