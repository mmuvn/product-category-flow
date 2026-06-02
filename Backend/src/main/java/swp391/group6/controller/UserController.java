package swp391.group6.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swp391.group6.dto.ResponseDTO;
import swp391.group6.dto.UserDTO;
import swp391.group6.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //get all users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    //get user by id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserDTO>> getUserById(@PathVariable long id) {
        try {
            Optional<UserDTO> user = userService.getUserById(id);
            if (user.isPresent()) {
                ResponseDTO<UserDTO> response = new ResponseDTO<>(200, "User retrieved successfully", user.get());
                return ResponseEntity.ok(response);
            } else {
                ResponseDTO<UserDTO> response = new ResponseDTO<>(404, "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ResponseDTO<UserDTO> response = new ResponseDTO<>(500, "Error retrieving user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    //create a new user
    @PostMapping
    public ResponseEntity<ResponseDTO<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            ResponseDTO<UserDTO> response = new ResponseDTO<>(201, "User created successfully", createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseDTO<UserDTO> response = new ResponseDTO<>(500, "Error creating user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    //update user via id
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserDTO>> updateUser(@PathVariable long id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            if (updatedUser != null) {
                ResponseDTO<UserDTO> response = new ResponseDTO<>(200, "User updated successfully", updatedUser);
                return ResponseEntity.ok(response);
            } else {
                ResponseDTO<UserDTO> response = new ResponseDTO<>(404, "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ResponseDTO<UserDTO> response = new ResponseDTO<>(500, "Error updating user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    //delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@PathVariable long id) {
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                ResponseDTO<Void> response = new ResponseDTO<>(200, "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                ResponseDTO<Void> response = new ResponseDTO<>(404, "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ResponseDTO<Void> response = new ResponseDTO<>(500, "Error deleting user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    //search user by name or email
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<?>> searchUsers(@RequestParam(required = false) String query,
                                                      @RequestParam(required = false) String email) {
        try {
            if (email != null && !email.isBlank()) {
                Optional<UserDTO> user = userService.getUserByEmail(email);
                if (user.isPresent()) {
                    ResponseDTO<UserDTO> response = new ResponseDTO<>(200, "User retrieved successfully", user.get());
                    return ResponseEntity.ok(response);
                } else {
                    ResponseDTO<UserDTO> response = new ResponseDTO<>(404, "User not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }

            if (query == null || query.isBlank()) {
                ResponseDTO<Void> response = new ResponseDTO<>(400, "Search query or email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            List<UserDTO> users = userService.searchUsers(query);
            ResponseDTO<List<UserDTO>> response = new ResponseDTO<>(200, "Search completed", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<Void> response = new ResponseDTO<>(500, "Error searching users");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
