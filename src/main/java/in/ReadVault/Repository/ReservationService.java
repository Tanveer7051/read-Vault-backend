package in.ReadVault.Repository;

import in.ReadVault.DTO.ReservationDTO;
import in.ReadVault.Entity.ReservationStatus;

import java.util.List;

public interface ReservationService {

    ReservationDTO createReservation(Long userId, Long bookId);

    List<ReservationDTO> getAllReservations();

    ReservationDTO getReservationById(Long id);

    List<ReservationDTO> getReservationsByUser(Long userId);

    void cancelReservation(Long reservationId, Long userId);

    ReservationDTO updateStatus(Long id, ReservationStatus status);

    void deleteReservation(Long id);
}