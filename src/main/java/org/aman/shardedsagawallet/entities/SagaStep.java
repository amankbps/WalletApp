package org.aman.shardedsagawallet.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.calcite.model.JsonType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="saga_step")
public class SagaStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="saga_instance_id",nullable = false)
    private Long sagaInstanceId;

    @Column(name="step_name",nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private SagaStepStatus status;

    @Column(name="error_message")
    private String errorMessage;

    //json step data
   @Type(JsonType.class)
    @Column(name="step_data",columnDefinition = "json")
    private String stepData;
}
