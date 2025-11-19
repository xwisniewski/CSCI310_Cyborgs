package com.example.csci310_teamproj.auth;

import static org.junit.Assert.*;

import com.example.csci310_teamproj.data.repository.UserRepositoryImpl;
import com.example.csci310_teamproj.domain.model.User;

import org.junit.Test;

import java.util.List;

public class UserRepositoryImplTest {

    @Test
    public void testRegisterUserAddsUserToList() {
        UserRepositoryImpl repo = new UserRepositoryImpl();

        User user = new User("123", "Alice", "alice@usc.edu");
        repo.registerUser(user);

        List<User> users = repo.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).getName());
        assertEquals("alice@usc.edu", users.get(0).getEmail());
        assertEquals("123", users.get(0).getId());
    }

    @Test
    public void testRegisterMultipleUsers() {
        UserRepositoryImpl repo = new UserRepositoryImpl();

        repo.registerUser(new User("1", "Bob", "bob@usc.edu"));
        repo.registerUser(new User("2", "Carol", "carol@usc.edu"));

        List<User> users = repo.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("Bob", users.get(0).getName());
        assertEquals("Carol", users.get(1).getName());
    }
}
