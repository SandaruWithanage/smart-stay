package lk.smartstay;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.identity.service.AccessManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthorizationHttpIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    @Autowired UserRepository users;
    @Autowired AccessRepository access;
    @Autowired AccessManagementService policies;
    @Autowired lk.smartstay.room.persistence.RoomRepository roomRepository;
    @Autowired lk.smartstay.guest.persistence.GuestRepository guestRepository;
    @Autowired lk.smartstay.reservation.service.BookingManagementService bookingService;

    private record Registered(String token, UUID guestId, UUID userId) { }

    private Registered register(String nickname) throws Exception {
        String email = "guest-" + UUID.randomUUID() + "@example.org";
        String doc = "P" + UUID.randomUUID().toString().replace("-", "");
        String body = mapper.writeValueAsString(java.util.Map.of(
            "fullName", nickname, "documentNumber", doc, "phone", "+94771234567",
            "email", email, "password", "SecretPassphrase123!"));
        var response = mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn().getResponse();
        String token = mapper.readTree(response.getContentAsString()).get("accessToken").asText();
        var profile = mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andReturn().getResponse();
        JsonNode data = mapper.readTree(profile.getContentAsString());
        UUID userId = UUID.fromString(data.get("userId").asText());
        return new Registered(token, users.findById(userId).orElseThrow().getGuestId(), userId);
    }

    @Test
    void unauthenticatedRequestsAndBadTokensAreRejected() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me").header("Authorization", "Bearer invalid.jwt.value"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/public/room-packages")).andExpect(status().isOk());
    }

    @Test
    void guestCannotAccessDeskGuestLookupOrStaffReservationLedger() throws Exception {
        Registered alice = register("Restricted Guest");
        mvc.perform(post("/api/guests/lookup")
            .header("Authorization","Bearer "+alice.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\\"documentNumber\\":\\"NIC-12345\\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/reservations")
            .header("Authorization","Bearer "+alice.token()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/me/reservations")
            .header("Authorization","Bearer "+alice.token()))
            .andExpect(status().isOk());
    }

    @Test
    void guestRoleCannotCreateRoomsOrReadOtherGuests() throws Exception {
        Registered alice = register("Alice Test");
        Registered bob = register("Bob Test");

        mvc.perform(get("/api/guests/" + alice.guestId())
            .header("Authorization", "Bearer " + alice.token()))
            .andExpect(status().isOk());
        mvc.perform(get("/api/guests/" + bob.guestId())
            .header("Authorization", "Bearer " + alice.token()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/rooms")
            .header("Authorization", "Bearer " + alice.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
              {"id":"R-201","roomType":"STANDARD","bedType":"QUEEN",
               "capacity":2,"operationalStatus":"ACTIVE"}
              """)).andExpect(status().isForbidden());
    }

    @Test
    void rolesAndNavigationAreResolvedFromDatabaseNotJwtClaims() throws Exception {
        Registered user = register("Policy Test");
        String token = user.token();
        mvc.perform(get("/api/me/ui-config").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.navigation[0].routeKey").value("workspace"))
            .andExpect(jsonPath("$.permissions").isArray());

        // Same token becomes ADMIN-authorized after a database role assignment.
        access.assignRole(user.userId(), "ADMIN");
        mvc.perform(get("/api/admin/access/permissions")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // Remove a normal ADMIN permission; existing token must lose access immediately.
        var updated = new java.util.HashSet<>(access.rolePermissions("ADMIN"));
        updated.remove("room.create");
        policies.replaceRolePermissions("ADMIN", updated);

        mvc.perform(post("/api/admin/rooms")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
              {"id":"R-202","roomType":"STANDARD","bedType":"QUEEN",
               "capacity":2,"operationalStatus":"ACTIVE"}
              """)).andExpect(status().isForbidden());

        // Test transaction rolls back role updates and user changes.
    }

    @Test
    void crossGuestReservationAndInvoiceAccessIsDenied() throws Exception {
        Registered alice = register("Alice Reservation");
        Registered bob = register("Bob Reservation");
        var room = new lk.smartstay.room.persistence.RoomEntity(
            "S-" + UUID.randomUUID().toString().substring(0, 8),
            lk.smartstay.room.domain.RoomType.STANDARD,
            lk.smartstay.room.domain.BedType.QUEEN, 2,
            lk.smartstay.room.domain.RoomOperationalStatus.ACTIVE);
        roomRepository.saveAndFlush(room);
        var booking = bookingService.create(new lk.smartstay.reservation.api.ReservationCreateRequest(
            bob.guestId(), room.getId(), lk.smartstay.room.domain.RoomType.STANDARD,
            java.time.LocalDate.of(2030, 5, 10), java.time.LocalDate.of(2030, 5, 12),
            2, true));
        UUID bookingId = booking.reservation().bookingId();

        mvc.perform(get("/api/reservations/" + bookingId)
            .header("Authorization", "Bearer " + alice.token()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/reservations/" + bookingId + "/invoice")
            .header("Authorization", "Bearer " + alice.token()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/reservations/" + bookingId)
            .header("Authorization", "Bearer " + bob.token()))
            .andExpect(status().isOk());
    }

    @Test
    void logoutRevokesCurrentTokenVersionImmediately() throws Exception {
        Registered user = register("Logout Test");
        mvc.perform(post("/api/auth/logout")
            .header("Authorization", "Bearer " + user.token()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + user.token()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void roleEditorPreventsDeletingAdminManagementPermission() {
        var altered = new java.util.HashSet<>(access.rolePermissions("ADMIN"));
        altered.remove("access.manage");
        assertThrows(IllegalArgumentException.class, () ->
            policies.replaceRolePermissions("ADMIN", altered));
    }
}
