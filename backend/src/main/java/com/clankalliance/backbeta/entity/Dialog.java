package com.clankalliance.backbeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table()
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dialog {

    @Id
    private String id;

    private Date time;

    @Lob
    private String content;

    //切断循环引用
    @ManyToOne
    @JsonIgnoreProperties("trainingDataList")
    private User sender;

    private String correction;

    private Double score;

}
