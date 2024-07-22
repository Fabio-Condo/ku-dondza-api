package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "interest") // Interesses do user
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    //@ManyToMany(mappedBy = "interests")
    //@JsonIgnoreProperties({"interests"})
    //private Set<User> users = new HashSet<>();

    public Interest() {
    }

    public Interest(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //public Set<User> getUsers() {
    //    return users;
    //}

    //public void setUsers(Set<User> users) {
    //    this.users = users;
    //}
}
