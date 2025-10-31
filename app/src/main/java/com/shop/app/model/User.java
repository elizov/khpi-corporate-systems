package com.shop.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    private String password;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email cannot be empty")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Role cannot be empty")
    @Column(nullable = false)
    private String role = "USER";

    @Min(value = 18, message = "Age must be at least 18 years old")
    @Max(value = 100, message = "Age must be no more than 100 years old")
    @Column(nullable = false)
    private Integer age;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
