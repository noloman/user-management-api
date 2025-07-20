package me.manulorenzo.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Role {
    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @Getter
    @Column(unique = true)
    private String name;
}
