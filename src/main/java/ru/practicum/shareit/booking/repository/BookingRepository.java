package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.advice.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b
            FROM Booking b
            join fetch b.item i
            join fetch i.owner o
            WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithItemAndOwner(@Param("id") Long bookingId);

    @Query("""
            SELECT b
            FROM Booking b
            join fetch b.item i
            join fetch i.owner o
            WHERE b.booker.id = :id
            """)
    Collection<Booking> findAllByUserId(@Param("id") Long userId);

    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE b.booker.id = :id
                        AND b.status = :status
                        AND b.start < :today
                        AND b.endTime > :today
            """)
    Collection<Booking> findAllByUserIdAndStatusAndStartBeforeAndEndAfter(@Param("id") Long userId,
                                                                          @Param("status") BookingStatus bookingStatus,
                                                                          @Param("today") LocalDateTime today);


    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE b.booker.id = :id
                        AND b.status = :status
                        AND b.endTime < :today
            """)
    Collection<Booking> findAllByUserIdAndStatusAndEndBefore(@Param("id") Long userId,
                                                             @Param("status") BookingStatus bookingStatus,
                                                             @Param("today") LocalDateTime today);

    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE b.booker.id = :id
                        AND b.status = :status
                        AND b.start > :today
            """)
    Collection<Booking> findAllByUserIdAndStatusAndStartAfter(@Param("id") Long userId,
                                                              @Param("status") BookingStatus bookingStatus,
                                                              @Param("today") LocalDateTime today);


    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE b.booker.id = :id
                        AND b.status = :status
            """)
    Collection<Booking> findAllByUserIdAndStatus(@Param("id") Long userId,
                                                 @Param("status") BookingStatus bookingStatus);

    @Query("""
            SELECT b
            FROM Booking b
            join fetch b.item i
            join fetch i.owner o
            WHERE o.id = :id
            """)
    Collection<Booking> findAllByOwnerId(@Param("id") Long userId);

    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE o.id = :id
                        AND b.status = :status
                        AND b.start < :today
                        AND b.endTime > :today
            """)
    Collection<Booking> findAllByOwnerIdAndStatusAndStartBeforeAndEndAfter(@Param("id") Long userId,
                                                                           @Param("status") BookingStatus bookingStatus,
                                                                           @Param("today") LocalDateTime today);


    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE o.id = :id
                        AND b.status = :status
                        AND b.endTime < :today
            """)
    Collection<Booking> findAllByOwnerIdAndStatusAndEndBefore(@Param("id") Long userId,
                                                              @Param("status") BookingStatus bookingStatus,
                                                              @Param("today") LocalDateTime today);

    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE o.id = :id
                        AND b.status = :status
                        AND b.start > :today
            """)
    Collection<Booking> findAllByOwnerIdAndStatusAndStartAfter(@Param("id") Long userId,
                                                               @Param("status") BookingStatus bookingStatus,
                                                               @Param("today") LocalDateTime today);


    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE o.id = :id
                        AND b.status = :status
            """)
    Collection<Booking> findAllByOwnerIdAndStatus(@Param("id") Long userId,
                                                  @Param("status") BookingStatus bookingStatus);

    @Query("""
                        SELECT b
                        FROM Booking b
                        join fetch b.item i
                        join fetch i.owner o
                        WHERE b.booker.id = :id
                        AND i.id = :itemId
                        AND b.status = :status
                        AND b.endTime < :today
            """)
    Optional<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(@Param("id") Long userId,
                                                                   @Param("itemId") Long itemId,
                                                                   @Param("status") BookingStatus status,
                                                                   @Param("today") LocalDateTime today);
}







