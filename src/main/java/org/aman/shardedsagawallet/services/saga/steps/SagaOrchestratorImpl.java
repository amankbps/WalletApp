package org.aman.shardedsagawallet.services.saga.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator{

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
    @Transactional
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
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {

        SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(()->new RuntimeException("Saga instance is not found"));

        SagaStepInterface step=sagaStepFactory.getSagaStep(stepName);
        if(step==null){
            log.error("Saga step is not found for step name {}",stepName);
            throw new RuntimeException("Saga step is not found");
        }


        SagaStep sagaStep=sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId,stepName,SagaStepStatus.PENDING)
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
            sagaStep.markAsRunning();
            sagaStepRepository.save(sagaStep); //updating the status to running in db
            boolean success=step.execute(sagaContext);

            if(success){
                sagaStep.markAsCompleted();
                sagaStepRepository.save(sagaStep);//updating the status to complete in db

                sagaInstance.setCurrentStep(stepName); //step just we completed
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance); //updating the status to running in db

                log.info("Step {} executed successfully",stepName);
                return true;
            }else{
                sagaStep.markAsFailed();
                sagaStepRepository.save(sagaStep);//updating the status to failed in db
                log.error("Step {} failed",stepName);
                return false;
            }

        } catch (JsonProcessingException e) {
            sagaStep.markAsFailed();
            sagaStepRepository.save(sagaStep);//updating the status to failed in db
            log.error("Step  {} failed",stepName);
            return false;
        }



    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        //STEP 1. Fetch the saga instance from db using the saga instance id
        //STEP 2. Fetch the saga step from db using the saga instance id and step name
        //STEP 3. Take the context form saga instance and call the
        //STEP 4. Update the appropriate status in the saga step

        SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(()->new RuntimeException("Saga instance is not found"));

        SagaStepInterface step=sagaStepFactory.getSagaStep(stepName);
        if(step==null){
            log.error("Saga step is not found for step name {}",stepName);
            throw new RuntimeException("Saga step is not found");
        }


        SagaStep sagaStep=sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId,stepName,SagaStepStatus.COMPLETED)
                .orElse(null
                //no such step found in db
                        );

        //if not found in db
        if(sagaStep.getId()==null){
            log.info("Step {} not found in db so it is already compensated",stepName);
             return true;
        }

        //to start execution we need saga context in json format
        try {
            SagaContext sagaContext=objectMapper.readValue(sagaInstance.getContext(),SagaContext.class);
            sagaStep.markAsCompensating();
            sagaStepRepository.save(sagaStep); //updating the status to running in db
            boolean success=step.compensate(sagaContext);

            if(success){
                sagaStep.markAsCompensated();
                sagaStepRepository.save(sagaStep);//updating the status to complete in db

                log.info("Step {} compensated successfully",stepName);
                return true;
            }else{
                sagaStep.markAsFailed();
                sagaStepRepository.save(sagaStep);//updating the status to failed in db
                log.error("Step {} failed",stepName);
                return false;
            }

        } catch (JsonProcessingException e) {
            sagaStep.markAsFailed();
            sagaStepRepository.save(sagaStep);//updating the status to failed in db
            log.error("Step  {} failed",stepName);
            return false;
        }


    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(()->new RuntimeException("Saga instance is not found"));
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(()->new RuntimeException("Saga instance is not found"));

        //mark the saga status as compensating in db
        sagaInstance.setStatus(SagaStatus.COMPENSATING);
        sagaInstanceRepository.save(sagaInstance);

        //get all the completed
        List<SagaStep>completedSteps=sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated=true;
        for(SagaStep completedStep:completedSteps){
              boolean compensated=this.compensateStep(sagaInstanceId,completedStep.getStepName());
              if(!compensated){
                  allCompensated=false;
              }
        }

         if(allCompensated){
              sagaInstance.setStatus(SagaStatus.COMPENSATED);
              sagaInstanceRepository.save(sagaInstance);
              log.info("Saga {} compensated successfully",sagaInstanceId);
         }else{
             log.error("Saga {} compensation failed",sagaInstanceId);
         }

    }

    @Override
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(()->new RuntimeException("Saga instance is not found"));
        sagaInstance.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(sagaInstance);
    }

    @Override
    public void completeSaga(Long sagaInstanceId) {
           SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId)
                   .orElseThrow(()->new RuntimeException("Saga instance is not found"));
           sagaInstance.setStatus(SagaStatus.COMPLETED);
           sagaInstanceRepository.save(sagaInstance);
    }
}
