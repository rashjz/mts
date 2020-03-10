package com.mts.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {
    @Id
    @GeneratedValue
    private Long accountId;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private User userId;
    private BigDecimal balance;
}
