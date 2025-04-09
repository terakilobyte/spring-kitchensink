package com.ongom.kitchensink.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ongom.kitchensink.model.Member;
import com.ongom.kitchensink.service.MemberService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for managing Member resources.
 */
@RestController
@RequestMapping("/rest/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * Handles GET requests to retrieve all members.
     * Corresponds to listAllMembers in the original JAX-RS service.
     *
     * @return A list of all members.
     */
    @GetMapping
    public List<Member> listAllMembers() {
        log.info("GET /members request received");
        return memberService.findAll();
    }

    /**
     * Handles GET requests to retrieve a member by ID.
     * Corresponds to lookupMemberById in the original JAX-RS service.
     *
     * @param id The ID of the member to retrieve.
     * @return ResponseEntity containing the member or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable String id) {
        log.info("GET /members/{} request received", id);
        return memberService.findById(id)
                .map(ResponseEntity::ok) // If found, return 200 OK with the member
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404 Not Found
    }

    /**
     * Handles POST requests to create a new member.
     * Corresponds to createMember in the original JAX-RS service.
     *
     * @param member The member data from the request body.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
         log.info("POST /members request received with body: {}", member);
        try {
            // The ID is typically null or ignored for creation, MongoDB generates it.
            // Ensure ID is null before passing to service if necessary, though service handles registration logic.
            member.setId(null);
            Member registeredMember = memberService.register(member);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredMember);
        } catch (ConstraintViolationException cve) {
            log.warn("Validation failed for member registration: {}", cve.getMessage());
            return ResponseEntity.badRequest().body(buildValidationErrors(cve));
        } catch (ValidationException ve) {
             log.warn("Business validation failed for member registration: {}", ve.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", ve.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            log.error("Error registering member: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Helper method to build a map of validation errors from a ConstraintViolationException.
     *
     * @param e The ConstraintViolationException.
     * @return A Map where keys are field names and values are error messages.
     */
    private Map<String, String> buildValidationErrors(ConstraintViolationException e) {
        return e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(), // Field name
                        ConstraintViolation::getMessage // Error message
                ));
    }
}
