package com.ongom.kitchensink.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ongom.kitchensink.model.Member;
import com.ongom.kitchensink.repository.MemberRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for registering new members.
 *
 * This class was translated from an EJB Stateless Session Bean.
 * Key changes:
 * - Replaced @Stateless with Spring's @Service annotation.
 * - Replaced @Inject with Spring's @Autowired constructor injection.
 * - Replaced JPA EntityManager with Spring Data MongoDB's MemberRepository.
 * - Replaced CDI Event with Spring's ApplicationEventPublisher.
 * - Switched from java.util.logging.Logger to SLF4J Logger.
 * - The persistence logic now uses memberRepository.save() for MongoDB.
 * - Event firing now uses applicationEventPublisher.publishEvent().
 * - Assumes a Member class exists, annotated as a Spring Data MongoDB @Document.
 * - Assumes a MemberRepository interface exists, extending MongoRepository<Member, String>.
 */
@Service // Marks this as a Spring service component
@RequiredArgsConstructor // Lombok annotation to generate constructor with required (final) fields
public class MemberService {

    private final MemberRepository memberRepository;
    private final Validator validator; // Inject the validator

    /**
     * Retrieves all members.
     *
     * @return A list of all members.
     */
    public List<Member> findAll() {
        return memberRepository.findAllByOrderByNameAsc();
    }

    /**
     * Finds a member by their ID.
     *
     * @param id The ID of the member.
     * @return An Optional containing the member if found.
     */
    public Optional<Member> findById(String id) {
        return memberRepository.findById(id);
    }

    /**
     * Finds a member by their email.
     *
     * @param email The email of the member.
     * @return An Optional containing the member if found.
     */
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    /**
     * Registers (creates) a new member after validation.
     *
     * @param member The member object to register.
     * @return The registered member with its generated ID.
     * @throws ConstraintViolationException if validation fails.
     * @throws ValidationException if the email is already registered.
     */
    public Member register(Member member) throws ConstraintViolationException, ValidationException {
        // Manual validation before saving
        validateMember(member);

        // Check if email already exists
        if (findByEmail(member.getEmail()).isPresent()) {
            throw new ValidationException("Email address " + member.getEmail() + " is already registered.");
        }

        // Persist the member
        Member newMember = memberRepository.save(member);
        return newMember;
    }

    /**
     * Validates the member object using Jakarta Bean Validation.
     *
     * @param member The member to validate.
     * @throws ConstraintViolationException If validation constraints are violated.
     */
    private void validateMember(Member member) throws ConstraintViolationException {
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
    }
}
