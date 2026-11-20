package lk.smartstay;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.reservation.persistence.BookingRequestRepository;
import lk.smartstay.reservation.persistence.ReservationRepository;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.persistence.RoomEntity;
import lk.smartstay.room.persistence.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingRequestHttpIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    @Autowired UserRepository users;
    @Autowired AccessRepository access;
    @Autowired RoomRepository rooms;
    @Autowired BookingRequestRepository requests;
    @Autowired ReservationRepository reservations;

    private record Registered(String token, UUID userId) { }

    private Registered register(String name) throws Exception {
        String email="booking-"+UUID.randomUUID()+"@example.org";
        String body=mapper.writeValueAsString(Map.of(
            "fullName",name,"documentNumber","P"+UUID.randomUUID().toString().replace("-",""),
            "phone","+94771234567","email",email,"password","LongTestPassword123!"));
        String token=mapper.readTree(mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString()).get("accessToken").asText();
        JsonNode profile=mapper.readTree(mvc.perform(get("/api/me")
            .header("Authorization","Bearer "+token)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        return new Registered(token, UUID.fromString(profile.get("userId").asText()));
    }

    private String room() {
        String id="BR-"+UUID.randomUUID().toString().substring(0,8);
        rooms.saveAndFlush(new RoomEntity(id, RoomType.STANDARD, BedType.QUEEN,
            2,RoomOperationalStatus.ACTIVE));
        return id;
    }

    private String submit(String token, String roomId) throws Exception {
        String payload=mapper.writeValueAsString(Map.of(
            "roomId",roomId,"checkIn","2030-08-10","checkOut","2030-08-12","guestCount",2));
        return mapper.readTree(mvc.perform(post("/api/booking-requests")
            .header("Authorization","Bearer "+token)
            .contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void guestRequestIsUnpaidAndDoesNotReserveRoomUntilStaffApproval() throws Exception {
        var guest=register("Requested Guest");
        var staff=register("Approving Staff");
        access.assignRole(staff.userId(),"ADMIN");
        String roomId=room();
        String requestId=submit(guest.token(),roomId);
        assertEquals(0, reservations.count());

        mvc.perform(get("/api/booking-requests")
            .header("Authorization","Bearer "+guest.token()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("PENDING"));
        mvc.perform(get("/api/admin/booking-requests?status=PENDING")
            .header("Authorization","Bearer "+guest.token()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/booking-requests/"+requestId+"/approve")
            .header("Authorization","Bearer "+guest.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"advanceReceived\":true}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/booking-requests/"+requestId+"/approve")
            .header("Authorization","Bearer "+staff.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"advanceReceived\":false}"))
            .andExpect(status().isBadRequest());

        String approved=mvc.perform(post("/api/admin/booking-requests/"+requestId+"/approve")
            .header("Authorization","Bearer "+staff.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"advanceReceived\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andReturn().getResponse().getContentAsString();
        assertEquals(1, reservations.count());
        assertNotNull(mapper.readTree(approved).get("reservationId"));
        assertTrue(mapper.readTree(approved).get("reservationId").isTextual());
        mvc.perform(post("/api/admin/booking-requests/"+requestId+"/approve")
            .header("Authorization","Bearer "+staff.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"advanceReceived\":true}"))
            .andExpect(status().isConflict());
    }

    @Test
    void rejectedAndWithdrawnRequestsNeverBecomeReservations() throws Exception {
        var guest=register("Withdrawing Guest");
        var staff=register("Rejecting Staff");
        access.assignRole(staff.userId(),"ADMIN");
        String roomId=room();
        String first=submit(guest.token(),roomId);
        mvc.perform(post("/api/booking-requests/"+first+"/withdraw")
            .header("Authorization","Bearer "+guest.token()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("WITHDRAWN"));

        String second=submit(guest.token(),roomId);
        mvc.perform(post("/api/admin/booking-requests/"+second+"/reject")
            .header("Authorization","Bearer "+staff.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reason\":\"Advance not received\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.reviewerNote").value("Advance not received"));
        assertEquals(0, reservations.count());
    }

    @Test
    void guestCannotWithdrawOrListAnotherUsersRequest() throws Exception {
        var alice=register("Alice Request");
        var bob=register("Bob Request");
        String requestId=submit(alice.token(),room());
        mvc.perform(post("/api/booking-requests/"+requestId+"/withdraw")
            .header("Authorization","Bearer "+bob.token()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/booking-requests")
            .header("Authorization","Bearer "+bob.token()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/booking-requests")).andExpect(status().isUnauthorized());
    }
}
