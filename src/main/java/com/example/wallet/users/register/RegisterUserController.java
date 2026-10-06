package com.example.wallet.users.register;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterUserController {

    private final RegisterUserUseCase registerUser;

    public RegisterUserController(RegisterUserUseCase registerUser) {
        this.registerUser = registerUser;
    }

    @PostMapping("/users")
    ResponseEntity<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        var user = registerUser.register(request.toCommand());
        return ResponseEntity.created(URI.create("/users/" + user.id())).body(RegisterUserResponse.from(user));
    }
}
