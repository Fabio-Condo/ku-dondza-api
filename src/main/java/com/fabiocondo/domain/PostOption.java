package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "post_option")
public class PostOption { // RENOMEAR PARA Enquete. LINKEDIN USA ESSE NOME

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String text;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_option",
            joinColumns = @JoinColumn(name = "option_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> peopleWhoSelected;

    public PostOption() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<User> getPeopleWhoSelected() {
        return peopleWhoSelected;
    }

    public void setPeopleWhoSelected(List<User> peopleWhoSelected) {
        this.peopleWhoSelected = peopleWhoSelected;
    }
}
