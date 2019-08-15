package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminService;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.SignoutService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/*This class implements the userDelete - "/admin/user/{userId}"*/
@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthorizationService authorizationService;

/*This end point is used to delete a user from the Quora application if the user has signed in and has valid user access token
 and has admin role. If any of these conditions are not met with, the corresponding exception is thrown
 Its a DELETE request. This endpoint requests path variable userId as a string for the corresponding user that needs
 to be deleted and accesstoken of the signed in user as a String in authorization Request Header
 It returns the uuid of the user that has been deleted and message in the JSON response with the corresponding HTTP status*/
    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable("userId") final String uuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        if(authorizationService.hasUserSignedIn(authorization)) {
            if(authorizationService.isUserAccessTokenValid(authorization)) {
                String UUID = adminService.deleteUser(uuid, authorization);
                final UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(UUID).status("USER SUCCESSFULLY DELETED");
                return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
            } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out");
            }
        } else {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

    }
}
