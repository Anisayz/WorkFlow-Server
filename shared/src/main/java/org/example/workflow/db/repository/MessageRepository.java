package org.example.workflow.db.repository;

import org.example.workflow.db.DatabaseManager;
import org.example.workflow.model.Message;

import java.util.*;
import java.util.stream.Collectors;

public class MessageRepository extends BaseRepository<Message> {

    public MessageRepository() {
        super(DatabaseManager.getInstance().getMessageHandler());
    }

    // ── BaseRepository ────────────────────────────────────────────────────────

    @Override
    public List<Message> findAll() {
        return Collections.unmodifiableList(
                new ArrayList<>(state.getMessages().values())
        );
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return state.getMessageById(id);
    }

    @Override
    public void save(Message message) {
        state.putMessage(message);
        persist();
    }

    @Override
    public void delete(UUID id) {
        state.removeMessage(id);
        persist();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Chat history for a team, oldest → newest.
     * @param limit  pass Integer.MAX_VALUE for full history
     */
    public List<Message> findByTeam(UUID teamId, int limit) {
        return state.getMessages().values().stream()
                .filter(m -> teamId.equals(m.getTeamId()))
                .sorted(Comparator.comparing(Message::getSentAt))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Message> findByTeam(UUID teamId) {
        return findByTeam(teamId, Integer.MAX_VALUE);
    }

    public List<Message> findBySender(UUID senderId) {
        return state.getMessages().values().stream()
                .filter(m -> senderId.equals(m.getSenderId()))
                .sorted(Comparator.comparing(Message::getSentAt))
                .collect(Collectors.toList());
    }

    public int getMessageCount(UUID teamId) {
        return (int) state.getMessages().values().stream()
                .filter(m -> teamId.equals(m.getTeamId()))
                .count();
    }
}