package org.example.workflow.db.repository;

import org.example.workflow.db.DatabaseManager;
import org.example.workflow.model.User;
import org.example.workflow.model.enums.Role;

import java.util.*;
import java.util.stream.Collectors;

public class UserRepository extends BaseRepository<User> {

    public UserRepository() {
        super(DatabaseManager.getInstance().getUserHandler());
    }

    // ── BaseRepository ────────────────────────────────────────────────────────

    @Override
    public List<User> findAll() {
        return List.copyOf(state.getUsers().values());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return state.getUserById(id);
    }

    @Override
    public void save(User user) {
        state.putUser(user);
        persist();
    }

    @Override
    public void delete(UUID id) {
        state.removeUser(id);
        persist();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return state.getUsers().values().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return state.getUsers().values().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
    }

    public boolean usernameExists(String username) {
        return state.getUsers().values().stream()
                .anyMatch(u -> username.equals(u.getUsername()));
    }

    public boolean emailExists(String email) {
        return state.getUsers().values().stream()
                .anyMatch(u -> email.equals(u.getEmail()));
    }

    public List<User> findByTeam(UUID teamId) {
        return state.getUsers().values().stream()
                .filter(u -> teamId.equals(u.getTeamId()))
                .collect(Collectors.toList());
    }

    public List<User> findByRole(Role role) {
        return state.getUsers().values().stream()
                .filter(u -> role == u.getRole())
                .collect(Collectors.toList());
    }

    /** Resolves a list of UUIDs to User objects (skips missing ones). */
    public List<User> findAllByIds(Collection<UUID> ids) {
        return ids.stream()
                .map(state::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}