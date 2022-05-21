package com.example.demo.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@ToString
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Teacher {
    @Id
    private int id;

    private String Name;
}