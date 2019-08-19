package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    @Autowired
    private UserDao userDao;

    //To check if the user with the access token is signed in / access token exists in the table
    //Returns boolean based on whether the access token is present in the table
    public boolean hasUserSignedIn (final String authorization) {
        return userDao.hasUserSignedIn(authorization);
    }

    /* Created initial for validating access token later combined into other methods
    //To check if the user has a valid acces token / access token exists and is valid
    //Returns boolean based on 2 factors: The expires_at time is greater than current time and LogoutAt is null
    public boolean isUserAccessTokenValid (final String authorization)  {
        return userDao.isUserAccessTokenValid(authorization);
    }
    */

    //Written isValidActiveAuthToken thrice once for CommonController, AdminController & QuestionController
    //Reason: Exception messages are slightly different
    //This implementation for CommonController
    public UserAuthTokenEntity isValidActiveAuthToken(final String authorization, Enum<ActionType> actionType) throws AuthorizationFailedException{
        return userDao.isValidActiveAuthToken(authorization, actionType);

    }

    //Written isValidActiveAuthToken thrice once for CommonController, AdminController & QuestionController
    //Reason: Exception messages are slightly different
    //This implementation for AdminController
    public UserAuthTokenEntity isValidActiveAuthTokenForAdmin(final String authorization) throws AuthorizationFailedException{
        return userDao.isValidActiveAuthTokenForAdmin(authorization);

    }
/*
    //Written isValidActiveAuthToken thrice once for CommonController, AdminController & QuestionController
    //Reason: Exception messages are slightly different
    //This implementation for QuestionController
    public UserAuthTokenEntity isValidActiveAuthTokenForQuestion(final String authorization, Enum<ActionType> actionType) throws AuthorizationFailedException{
        return userDao.isValidActiveAuthTokenForQuestion(authorization,actionType);

    }
*/
    //To fetch UserAuthTokenEntity for particular acces token
    public UserAuthTokenEntity fetchAuthTokenEntity(final String authorization) throws SignOutRestrictedException {
        final UserAuthTokenEntity fetchedUserAuthTokenEntity = userDao.getUserAuthToken(authorization);
        return fetchedUserAuthTokenEntity;
    }



}
