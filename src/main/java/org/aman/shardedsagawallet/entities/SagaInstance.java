package org.aman.shardedsagawallet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import org.apache.calcite.model.JsonType;




@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="saga_instance")
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private SagaStatus status=SagaStatus.STARTED;

    @Type(JsonType.class)
    @Column(name="context",columnDefinition = "json")
    private String context;

    @Column(name="current_step")
    private String currentStep;


}
