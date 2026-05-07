package in.ReadVault.Controller;


import in.ReadVault.DTO.ReservationDTO;
import in.ReadVault.Entity.ReservationStatus;
import in.ReadVault.Entity.User;
import in.ReadVault.Repository.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

    @RestController
    @RequestMapping("/api/reservations")
    @RequiredArgsConstructor
    public class ReservationsController {

        private final ReservationService reservationService;

        private Long getUserId(Authentication authentication) {
            return ((User) authentication.getPrincipal()).getId();
        }


        @PostMapping("/{bookId}")
        public ResponseEntity<ReservationDTO> createReservation(
                @PathVariable Long bookId,
                Authentication authentication) {

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(reservationService.createReservation(getUserId(authentication), bookId));
        }

        @GetMapping("/my")
        public ResponseEntity<List<ReservationDTO>> getMyReservations(Authentication authentication) {
            return ResponseEntity.ok(
                    reservationService.getReservationsByUser(getUserId(authentication)));
        }

        @GetMapping
        public ResponseEntity<List<ReservationDTO>> getAllReservations() {
            return ResponseEntity.ok(reservationService.getAllReservations());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ReservationDTO> getById(@PathVariable Long id) {
            return ResponseEntity.ok(reservationService.getReservationById(id));
        }

        @PutMapping("/{id}/cancel")
        public ResponseEntity<Map<String, Object>> cancelReservation(
                @PathVariable Long id,
                Authentication authentication) {

            reservationService.cancelReservation(id, getUserId(authentication));

            return ResponseEntity.ok(Map.of(
                    "message", "Reservation cancelled successfully",
                    "timeStamp", LocalDate.now()
            ));
        }

        @PutMapping("/{id}/status")
        public ResponseEntity<ReservationDTO> updateStatus(
                @PathVariable Long id,
                @RequestParam ReservationStatus status) {

            return ResponseEntity.ok(reservationService.updateStatus(id, status));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Map<String, Object>> deleteReservation(@PathVariable Long id) {

            reservationService.deleteReservation(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Reservation deleted successfully",
                    "timeStamp", LocalDate.now()
            ));
        }
    }
