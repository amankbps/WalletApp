package org.aman.shardedsagawallet.services.saga.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.entities.SagaInstance;
import org.aman.shardedsagawallet.entities.SagaStatus;
import org.aman.shardedsagawallet.entities.SagaStep;
import org.aman.shardedsagawallet.entities.SagaStepStatus;
import org.aman.shardedsagawallet.repositories.SagaInstanceRepository;
import org.aman.shardedsagawallet.repositories.SagaStepRepository;
import org.aman.shardedsagawallet.services.saga.SagaContext;
import org.aman.shardedsagawallet.services.saga.SagaStepInterface;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator{

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
    public Long startSaga(SagaContext context) {
        try {
            //convert the context to a json as a string
            String contextJson=objectMapper.writeValueAsString(context);
            SagaInstance sagaInstance=SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();
            sagaInstance=sagaInstanceRepository.save(sagaInstance);

            log.info("Started SAGA with ID {}",sagaInstance.getId());
            return sagaInstance.getId();

        }catch (Exception e){
            log.error("Error starting the SAGA ",e);
            throw new RuntimeException("Error Starting saga",e);
        }


    }

    @Override
    public boolean executeStep(Long sagaInstanceId, String stepName) {

        SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(()->new RuntimeException("Saga instance is not found"));

        SagaStepInterface step=sagaStepFactory.getSagaStep(stepName);
        if(step==null){
            log.error("Saga step is not found for step name {}",stepName);
            throw new RuntimeException("Saga step is not found");
        }

        SagaStep sagaStep=sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, SagaStepStatus.PENDING)
                .stream()
                .filter(s->s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder()
                        .sagaInstanceId(sagaInstanceId)
                        .stepName(stepName)
                        .status(SagaStepStatus.PENDING)
                        .build());

         //if not found in db
          if(sagaStep.getId()==null){
              sagaStepRepository.save(sagaStep);
          }

          //to start execution we need saga context in json format
        try {
            SagaContext sagaContext=objectMapper.readValue(sagaInstance.getContext(),SagaContext.class);
            sagaStep.setStatus(SagaStepStatus.RUNNING);
            sagaStepRepository.save(sagaStep); //updating the status to running in db
            boolean success=step.execute(sagaContext);

            if(success){
                sagaStep.setStatus(SagaStepStatus.COMPLETED);
                sagaStepRepository.save(sagaStep);//updating the status to complete in db

                sagaInstance.setCurrentStep(stepName); //step just we completed
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance); //updating the status to running in db

                log.info("Step {} executed successfully",stepName);
                return true;
            }else{
                sagaStep.setStatus(SagaStepStatus.FAILED);
                sagaStepRepository.save(sagaStep);//updating the status to failed in db
                log.error("Step {} failed",stepName);
                return false;
            }

        } catch (JsonProcessingException e) {
            sagaStep.setStatus(SagaStepStatus.FAILED);
            sagaStepRepository.save(sagaStep);//updating the status to failed in db
            log.error("Step  {} failed",stepName);
            return false;
        }



    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
