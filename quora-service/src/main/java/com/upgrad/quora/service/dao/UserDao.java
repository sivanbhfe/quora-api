package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Persisting user entity
    public UserEntity createUser (UserEntity userEntity){
        entityManager.persist(userEntity);
        return userEntity;
    }
    public UserEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", UserEntity.class)
                    .setParameter("email", email).getSingleResult();
        } catch (NoResultException exc) {
            return null;
        }

    }

    public UserEntity getUserByUsername(final String username) {
        try {
            return entityManager.createNamedQuery("userByUsername", UserEntity.class)
                    .setParameter("username", username).getSingleResult();
        } catch (NoResultException exc) {
            return null;
        }

    }

    public UserEntity getUserById(final String uuid) {
        try {
            return entityManager.createNamedQuery("userByUuid", UserEntity.class)
                    .setParameter("uuid", uuid).getSingleResult();
        } catch (Exception exc) {
            return null;
        }

    }

    public UserAuthTokenEntity createAuthToken(final UserAuthTokenEntity userAuthTokenEntity) {
        entityManager.persist(userAuthTokenEntity);
        return userAuthTokenEntity;
    }

    public String signOut(final String accessToken) throws SignOutRestrictedException {
        UserAuthTokenEntity userAuthTokenEntity = entityManager.createNamedQuery("userByAccessToken", UserAuthTokenEntity.class)
                .setParameter("accessToken", accessToken).getSingleResult();
        final ZonedDateTime now = ZonedDateTime.now();
        if(userAuthTokenEntity!=null && userAuthTokenEntity.getLogoutAt()==null && userAuthTokenEntity.getExpiresAt().compareTo(now)>=0){
            Integer userId = userAuthTokenEntity.getUser().getId();
            userAuthTokenEntity.setLogoutAt(now);
            entityManager.merge(userAuthTokenEntity);
            UserEntity userEntity = entityManager.createNamedQuery("userById", UserEntity.class)
                    .setParameter("id", userId).getSingleResult();
            return userEntity.getUuid();

        } else {
            throw new SignOutRestrictedException("SGR-001","User is not Signed in");
        }
    }

    public boolean hasUserSignedIn(final String accessToken) {
        UserAuthTokenEntity userAuthTokenEntity = entityManager.createNamedQuery("userByAccessToken", UserAuthTokenEntity.class)
                .setParameter("accessToken", accessToken).getSingleResult();
        if(userAuthTokenEntity!=null){
            return true;
        } else {
            return false;
        }
    }

    public boolean isUserAccessTokenValid(final String accessToken) {
        UserAuthTokenEntity userAuthTokenEntity = entityManager.createNamedQuery("userByAccessToken", UserAuthTokenEntity.class)
                .setParameter("accessToken", accessToken).getSingleResult();
        final ZonedDateTime now = ZonedDateTime.now();
        if(userAuthTokenEntity.getLogoutAt()==null && userAuthTokenEntity.getExpiresAt().compareTo(now)>=0){
            return true;
        } else {
            return false;
        }
    }

    public boolean isRoleAdmin(final String accessToken) {
        UserAuthTokenEntity userAuthTokenEntity = entityManager.createNamedQuery("userByAccessToken", UserAuthTokenEntity.class)
                .setParameter("accessToken", accessToken).getSingleResult();
        UserEntity userEntity = userAuthTokenEntity.getUser();
        if(userEntity.getRole()=="admin"){
            return true;
        } else {
            return false;
        }
    }
    public void updateUser(final UserEntity updatedUserEntity) {
        entityManager.merge(updatedUserEntity);
    }

    public UserAuthTokenEntity getUserAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("userAuthTokenByAccessToken", UserAuthTokenEntity.class)
                    .setParameter("accessToken", accessToken).getSingleResult();
        } catch(NoResultException exc){
            return null;
        }

    }
}
