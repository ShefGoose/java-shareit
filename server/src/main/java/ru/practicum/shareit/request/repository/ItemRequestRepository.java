package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;


public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("""
            SELECT ir
            FROM ItemRequest ir
            join fetch ir.requestor
            WHERE ir.requestor.id = :requestorId
            ORDER BY ir.created DESC
            """)
    Page<ItemRequest> findAllByRequestor_Id(Long requestorId, Pageable pageable);

    @Query("""
            SELECT ir
            FROM ItemRequest ir
            join fetch ir.requestor
            WHERE ir.requestor.id <> :requestorId
            ORDER BY ir.created DESC
            """)
    Page<ItemRequest> findAllByRequestor_IdNot(Long requestorId, Pageable pageable);
}
