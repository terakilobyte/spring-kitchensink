package com.ongom.kitchensink.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ongom.kitchensink.model.Member;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Member document.
 *
 * This interface replaces the original JPA-based MemberRepository.
 * Spring Data MongoDB automatically implements the methods defined in this interface
 * based on their names or explicit @Query annotations (if needed).
 *
 * Assumes the existence of a com.ongom.kitchensink.model.Member class annotated with
 * @org.springframework.data.mongodb.core.mapping.Document and containing an
 * @org.springframework.data.annotation.Id field of type Long. Note that using String
 * as the ID type is more common with MongoDB. Adjust the ID type parameter
 * <Member, Long> if the Member document uses a different ID type.
 */
@Repository
public interface MemberRepository extends MongoRepository<Member, String> {

    /**
     * Finds a Member by its email address.
     * Corresponds to the original findByEmail method which used JPA Criteria API.
     * Spring Data derives the query from the method name.
     *
     * @param email The email address to search for.
     * @return An Optional containing the found Member, or empty if not found.
     */
    Optional<Member> findByEmail(String email);

    /**
     * Finds all Member documents and orders them by name ascending.
     * Corresponds to the original findAllOrderedByName method which used JPA Criteria API.
     * Spring Data derives the query and sorting from the method name.
     *
     * @return A List of all Member documents, ordered by name.
     */
    List<Member> findAllByOrderByNameAsc();

    // Standard CRUD methods like findById, save, deleteById etc. are inherited
    // from MongoRepository<Member, Long> and don't need to be explicitly declared.
    // The original findById(Long id) is covered by the inherited findById(Long id)
    // which returns Optional<Member>.
}
