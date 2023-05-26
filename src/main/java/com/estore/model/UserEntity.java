package com.estore.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


/**
 * {@link UserEntity}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("e_store.user")
public class UserEntity {

    @Id
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private UserRole role;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column
    private String email;

    @Column
    private String phone;

    @ToString.Include(name = "password")
    private String hidePassword() {
        return "*****";
    }

}
