package com.clankalliance.backbeta.entity.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_data_dialogs")
public class TrainingDataDialogs {

    @Id
    @Column(name = "training_data_id")
    private String trainingDataId;

    @Column(name = "dialogs_id")
    private String dialogsId;


}
