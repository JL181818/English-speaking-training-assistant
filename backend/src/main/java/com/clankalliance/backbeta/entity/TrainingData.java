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
    @JoinColumn(name = "user_id")
    private User user;

    private double score;

    @OneToMany
    @JoinTable(name = "training_data_dialogs",
            joinColumns = {@JoinColumn(name = "training_data_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "dialogs_id", referencedColumnName = "id")}
    )
    private List<Dialog> dialogs;

}
