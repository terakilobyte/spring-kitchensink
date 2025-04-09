package com.ongom.kitchensink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ongom.kitchensink.model.Member;
import com.ongom.kitchensink.service.MemberService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the MemberController using standalone MockMvc setup.
 * This approach avoids loading the Spring context slice used by @WebMvcTest
 * and uses Mockito directly for mocking and injection.
 */
@ExtendWith(MockitoExtension.class) // Initialize Mockito mocks
class MemberControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private Member validMemberInput;
    private Member expectedMemberOutput;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
        objectMapper = new ObjectMapper();

        validMemberInput = new Member("Jane Doe", "jane.doe@example.com", "0987654321");

        expectedMemberOutput = new Member("Jane Doe", "jane.doe@example.com", "0987654321");
    }

    @Test
    void createMember_whenValidInput_shouldReturnCreated() throws Exception {
        when(memberService.register(any(Member.class))).thenReturn(expectedMemberOutput);

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMemberInput)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(expectedMemberOutput.getId())))
                .andExpect(jsonPath("$.name", is(validMemberInput.getName())))
                .andExpect(jsonPath("$.email", is(validMemberInput.getEmail())))
                .andExpect(jsonPath("$.phoneNumber", is(validMemberInput.getPhoneNumber())));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberService, times(1)).register(memberCaptor.capture());
    }

    @Test
    void createMember_whenEmailExists_shouldReturnConflict() throws Exception {
        when(memberService.register(any(Member.class)))
                .thenThrow(new ValidationException("Email address " + validMemberInput.getEmail() + " is already registered."));

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMemberInput)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Email address " + validMemberInput.getEmail() + " is already registered.")));

        verify(memberService, times(1)).register(any(Member.class));
    }

    @Test
    void createMember_whenValidationFails_shouldReturnBadRequest() throws Exception {
        Member invalidMemberInput = new Member("Jane123", "jane.doe@example.com", "0987654321");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Member> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("Must not contain numbers");

        Set<ConstraintViolation<?>> violations = new HashSet<>(Collections.singletonList(violation));
        ConstraintViolationException constraintViolationException = new ConstraintViolationException("Validation failed", violations);

        when(memberService.register(any(Member.class))).thenThrow(constraintViolationException);

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMemberInput)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Must not contain numbers")));

        verify(memberService, times(1)).register(any(Member.class));
    }

     @Test
    void createMember_whenPhoneNumberInvalid_shouldReturnBadRequest() throws Exception {
        Member invalidMemberInput = new Member("Jane Doe", "jane.doe@example.com", "invalid-phone");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Member> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("phoneNumber");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("Must contain only numbers");

        Set<ConstraintViolation<?>> violations = new HashSet<>(Collections.singletonList(violation));
        ConstraintViolationException constraintViolationException = new ConstraintViolationException("Validation failed", violations);

        when(memberService.register(any(Member.class))).thenThrow(constraintViolationException);

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMemberInput)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.phoneNumber", is("Must contain only numbers")));

        verify(memberService, times(1)).register(any(Member.class));
    }
}
