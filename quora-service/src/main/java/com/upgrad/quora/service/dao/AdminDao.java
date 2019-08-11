package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;


@Repository
public class AdminDao {

    @PersistenceContext
    private EntityManager entityManager;


    public UserEntity deleteUser(final String uuid) {
        try {
            return entityManager.createNamedQuery("deleteUserByUuid", UserEntity.class)
                    .setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException exc) {
            return null;
        }

    }
}
