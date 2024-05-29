package com.clankalliance.backbeta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table()
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    //为避免long传向前台出现精度丢失，通过注释将long化为String
    @Id
    @JsonIgnore
    private long id;

    @Size(max = 50)
    private String nickName;

    @Column(unique = true)
    @JsonSerialize(using= ToStringSerializer.class)
    private long phone;

    //男：false 女：true
    private Boolean gender;

    //切断循环引用
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    @JsonIgnoreProperties("user")
    private List<TrainingData> trainingDataList;


}
