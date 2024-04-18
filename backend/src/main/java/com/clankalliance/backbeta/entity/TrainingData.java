package com.clankalliance.backbeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table()
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingData {

    @Id
    private String id;

    private Date time;

    @ManyToOne
    private User user;

    private double score;

    @OneToMany
    private List<Dialog> dialogs;

}
