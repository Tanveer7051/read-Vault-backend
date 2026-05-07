package in.ReadVault.Service;

import in.ReadVault.DTO.ReservationDTO;
import in.ReadVault.Entity.*;
import in.ReadVault.GlobalExceptionHandling.*;
import in.ReadVault.Repository.BookRepository;
import in.ReadVault.Repository.ReservationRepository;
import in.ReadVault.Repository.ReservationService;
import in.ReadVault.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ReservationDTO createReservation(Long userId, Long bookId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        if (book.getBookType() == BookType.DIGITAL) {
            throw new DigitalBookDoesNotHaveCopiesException("Digital books do not require reservation");
        }

        if (book.getAvailableCopies() > 0) {
            throw new BadRequestExceptions("Book is available. Borrow it directly.");
        }

        boolean exists = reservationRepository
                .existsByUserIdAndBookIdAndStatus(userId, bookId, ReservationStatus.PENDING);

        if (exists) {
            throw new BadRequestExceptions("You already have an active reservation");
        }

        long queuePosition = reservationRepository
                .countByBookIdAndStatus(bookId, ReservationStatus.PENDING) + 1;

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setExpiryDate(LocalDate.now().plusDays(3));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setQueuePosition((int) queuePosition);

        return mapToDTO(reservationRepository.save(reservation));
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservationDTOS= reservationRepository.findAll();


        if(reservationDTOS.isEmpty()){
            throw new ReservationNotFoundException("Reservation not found");
        }

        return reservationDTOS
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public ReservationDTO getReservationById(Long id) {
        return mapToDTO(reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found")));
    }

    @Override
    public List<ReservationDTO> getReservationsByUser(Long userId) {
        List<Reservation> reservations= reservationRepository.findByUserId(userId);

        if(reservations.isEmpty()){
            throw new ReservationNotFoundException("Reservation not found");
        }
        return reservations
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedExceptions("Not allowed to cancel");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestExceptions("Only pending reservations can be cancelled");
        }

        int removedPosition = reservation.getQueuePosition();
        Long bookId = reservation.getBook().getId();

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        List<Reservation> queue = reservationRepository
                .findByBookIdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.PENDING);

        for (Reservation r : queue) {
            if (r.getQueuePosition() > removedPosition) {
                r.setQueuePosition(r.getQueuePosition() - 1);
            }
        }

        reservationRepository.saveAll(queue);
    }


    @Override
    public ReservationDTO updateStatus(Long id, ReservationStatus status) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestExceptions("Cannot update cancelled reservation");
        }

        reservation.setStatus(status);

        return mapToDTO(reservationRepository.save(reservation));
    }


    @Override
    public void deleteReservation(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        reservationRepository.delete(reservation);
    }

    private ReservationDTO mapToDTO(Reservation r) {
        return new ReservationDTO(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getBook().getId(),
                r.getBook().getTitle(),
                r.getCreatedDate(),
                r.getExpiryDate(),
                r.getStatus()
        );
    }
}