package com.ongom.kitchensink.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a Member entity, migrated from JPA to Spring Data MongoDB.
 *
 * <p>This class is now annotated with {@link Document} to mark it as a MongoDB document
 * persisted in the "members" collection.</p>
 *
 * <p>The original JPA {@code @GeneratedValue} strategy for {@code id} has been replaced
 * with MongoDB's default ObjectId generation, mapped to a {@code String} type.
 * Bean Validation annotations ({@code @NotNull}, {@code @Size}, {@code @Email}, etc.)
 * are retained for use in application layers (e.g., REST controllers).</p>
 *
 * <h2>Schema Recommendations:</h2>
 * <ul>
 *   <li><b>Embedding vs. Referencing:</b> The original entity had no relationships defined.
 *       If relationships were added (e.g., to Orders or Addresses), carefully consider
 *       embedding vs. referencing. For data frequently accessed together with the Member
 *       (like a primary address), embedding can improve read performance. For potentially
 *       large collections (like many orders) or data updated independently, referencing
 *       (storing IDs of related documents) is generally preferred to avoid data duplication
 *       and maintain consistency.</li>
 *   <li><b>Indexing:</b> Indexes significantly improve query performance on the indexed fields
 *       at the cost of slightly slower writes and increased storage usage.
 *       <ul>
 *         <li>The {@code email} field is marked with {@code @Indexed(unique = true)} as it
 *             was unique in the source and is commonly used for lookups or login.</li>
 *         <li>The {@code name} field is recommended for indexing ({@code @Indexed}) if members
 *             are frequently searched or sorted by name.</li>
 *         <li>The {@code phoneNumber} field would recommended for indexing ({@code @Indexed})
 *             if lookups by phone number are common.</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Manual Review Areas:</b></p>
 * <ul>
 *  <li>Ensure the collection name "members" is appropriate for your MongoDB schema.</li>
 *  <li>Review the chosen indexes based on actual query patterns in the application.</li>
 *  <li>The {@code Serializable} interface and {@code @XmlRootElement} annotation were removed
 *      as they are less commonly required in Spring Boot/MongoDB JSON-based applications.
 *      Re-evaluate if specific serialization or XML use cases exist.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@Document(collection = "members")
public class Member {

    @Id
    private String id; // Changed from Long to String for MongoDB ObjectId compatibility

    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    @Indexed // Recommended index for searching/sorting by name
    private String name;

    @NotNull
    @NotEmpty
    @Email
    @Indexed(unique = true) // Enforce uniqueness and optimize lookups by email
    private String email;

    @NotNull
    @Size(min = 10, max = 12)
    @Digits(fraction = 0, integer = 12) // Validates that it contains only digits
    @Pattern(regexp = "[0-9]+", message = "Must contain only numbers")
    @Field("phone_number") // Maps to the 'phone_number' field in MongoDB
    private String phoneNumber;

    public Member(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
