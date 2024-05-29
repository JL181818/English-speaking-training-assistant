package com.clankalliance.backbeta.entity.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TrainingDataDialogs.class)
@Entity
@Table(name = "training_data_dialogs")
public class TrainingDataDialogs implements Serializable {

    @Id
    @Column(name = "training_data_id")
    private String trainingDataId;

    @Id
    @Column(name = "dialogs_id")
    private String dialogsId;


}
