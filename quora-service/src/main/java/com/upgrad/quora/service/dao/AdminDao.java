package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;


@Repository
public class AdminDao {

    @PersistenceContext
    private EntityManager entityManager;


    public String deleteUser(final String uuid) throws UserNotFoundException{
        try {
            UserEntity deletedUserEntity = entityManager.createNamedQuery("userByUuid", UserEntity.class)
                    .setParameter("uuid", uuid).getSingleResult();
            Integer deletedUserId = deletedUserEntity.getId();
            String deletedUserUuid = uuid;
            //Running remove only on UserEntity
            // All other related table entries will be deleted by @OnDelete annotation function defined for all foregin key fields
            entityManager.remove(deletedUserEntity);
            return deletedUserUuid;
        } catch (NullPointerException exc) {
            return null;
           // throw new UserNotFoundException("USR-001","User with entered uuid to be deleted does not exist");
        }
    }

}
