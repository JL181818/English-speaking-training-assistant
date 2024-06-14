package com.clankalliance.backbeta.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Word {

    @Id
    private String word;

    @Lob
    private String trans;

    @Lob
    private String example;

    @Lob
    private String exampleTrans;

}
