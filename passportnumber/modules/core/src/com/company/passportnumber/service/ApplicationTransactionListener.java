package com.company.passportnumber.service;

import com.company.passportnumber.entity.FraudDetectionFlag;
import com.haulmont.cuba.core.entity.StandardEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.listener.BeforeCommitTransactionListener;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.entity.Entity;

import javax.validation.ValidationException;
import java.util.Collection;

@Component("passportnumber_ApplicationTransactionListener")
public class ApplicationTransactionListener implements BeforeCommitTransactionListener {

    private Logger log = LoggerFactory.getLogger(ApplicationTransactionListener.class);

    @Override
    public void beforeCommit(EntityManager entityManager, Collection<Entity> managedEntities) {
        for (Entity entity : managedEntities)
            if (entity instanceof StandardEntity
                    && !((StandardEntity) entity).isDeleted()
                    && entity.getClass().isAnnotationPresent(FraudDetectionFlag.class)
                    && !fraudDetectorFeedAndFastCheck(entity))
            {
                logFraudDetectionFailure(log, entity);
                String msg = String.format("Fraud detection failure in '%s' object with id = '%s'",
                        entity.getClass().getSimpleName(), entity.getId() );
                throw new ValidationException(msg);
            }
    }

    private boolean fraudDetectorFeedAndFastCheck(Entity en) {
        return true;
    }

    private void logFraudDetectionFailure(Logger log, Entity entity) {

    }
}