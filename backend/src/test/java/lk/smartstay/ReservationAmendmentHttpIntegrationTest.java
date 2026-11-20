package lk.smartstay;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.reservation.api.ReservationCreateRequest;
import lk.smartstay.reservation.service.BookingManagementService;
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
import tools.jackson.databind.json.JsonMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationAmendmentHttpIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired UserRepository users;
    @Autowired AccessRepository access;
    @Autowired RoomRepository rooms;
    @Autowired BookingManagementService booking;

    private record Account(UUID id,UUID guestId,String token) { }

    private Account register() throws Exception {
        String email="amend-"+UUID.randomUUID()+"@example.org";
        var input=json.writeValueAsString(Map.of("fullName","Amend Guest",
            "documentNumber","P"+UUID.randomUUID().toString().replace("-",""),
            "phone","+94771234567","email",email,"password","VeryStrongTestPassword123!"));
        var response=mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON).content(input))
            .andExpect(status().isCreated()).andReturn().getResponse();
        String token=json.readTree(response.getContentAsString()).get("accessToken").asText();
        var me=mvc.perform(get("/api/me").header("Authorization","Bearer "+token))
            .andExpect(status().isOk()).andReturn().getResponse();
        UUID userId=UUID.fromString(json.readTree(me.getContentAsString()).get("userId").asText());
        return new Account(userId,users.findById(userId).orElseThrow().getGuestId(),token);
    }

    private UUID bookFor(Account account) {
        String roomId="A-"+UUID.randomUUID().toString().substring(0,8);
        rooms.saveAndFlush(new RoomEntity(roomId,RoomType.STANDARD,BedType.QUEEN,2,RoomOperationalStatus.ACTIVE));
        var receipt=booking.create(new ReservationCreateRequest(
            account.guestId(),roomId,RoomType.STANDARD,
            LocalDate.of(2030,9,10),LocalDate.of(2030,9,12),2,true));
        return receipt.reservation().bookingId();
    }

    @Test
    void ownGuestCanAmendButCannotAmendAnotherGuestsReservation() throws Exception {
        Account alice=register();
        Account bob=register();
        UUID target=bookFor(bob);
        String request=json.writeValueAsString(Map.of(
            "checkIn","2030-09-10","checkOut","2030-09-15","guestCount",2));
        mvc.perform(put("/api/reservations/"+target)
            .header("Authorization","Bearer "+alice.token())
            .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/reservations/"+target+"/amendments")
            .header("Authorization","Bearer "+alice.token()))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/reservations/"+target)
            .header("Authorization","Bearer "+bob.token())
            .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservation.checkOut").value("2030-09-15"))
            .andExpect(jsonPath("$.invoice.nights").value(5));
        mvc.perform(get("/api/reservations/"+target+"/amendments")
            .header("Authorization","Bearer "+bob.token()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].previousGuestCount").value(2));
        mvc.perform(put("/api/reservations/"+target)
            .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCanAmendAfterDatabaseRoleAssignment() throws Exception {
        Account owner=register();
        Account staff=register();
        access.assignRole(staff.id(),"ADMIN");
        UUID bookingId=bookFor(owner);
        mvc.perform(put("/api/reservations/"+bookingId)
            .header("Authorization","Bearer "+staff.token())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of(
                "checkIn","2030-09-10","checkOut","2030-09-12","guestCount",3))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservation.guestCount").value(3));
    }
}
