package com.company.passportnumber.service;

import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.listener.BeforeCommitTransactionListener;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.entity.Entity;
import java.util.Collection;

@Component("passportnumber_ApplicationTransactionListener")
public class ApplicationTransactionListener implements BeforeCommitTransactionListener {


    @Override
    public void beforeCommit(EntityManager entityManager, Collection<Entity> managedEntities) {

    }


}