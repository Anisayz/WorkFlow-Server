    package org.example.workflow.db.repository;

    import org.example.workflow.db.DatabaseManager;
    import org.example.workflow.model.Team;
    import org.example.workflow.model.User;

    import java.util.*;
    import java.util.stream.Collectors;

    public class TeamRepository extends BaseRepository<Team> {

        private final UserRepository userRepository;

        public TeamRepository() {
            super(DatabaseManager.getInstance().getTeamHandler());
            this.userRepository = new UserRepository();
        }

        // ── BaseRepository ────────────────────────────────────────────────────────

        @Override
        public List<Team> findAll() {
            return List.copyOf(state.getTeams().values());
        }

        @Override
        public Optional<Team> findById(UUID id) {
            return state.getTeamById(id);
        }

        @Override
        public void save(Team team) {
            state.putTeam(team);
            persist();
        }

        @Override
        public void delete(UUID id) {
            state.removeTeam(id);
            persist();
        }

        // ── Queries ───────────────────────────────────────────────────────────────

        public Optional<Team> findByInviteCode(String inviteCode) {
            return state.getTeams().values().stream()
                    .filter(t -> inviteCode.equals(t.getInviteCode()))
                    .findFirst();
        }

        public Optional<Team> findByLeader(UUID leaderId) {
            return state.getTeams().values().stream()
                    .filter(t -> leaderId.equals(t.getLeaderId()))
                    .findFirst();
        }

        public List<Team> findByMember(UUID userId) {
            return state.getTeams().values().stream()
                    .filter(t -> t.hasMember(userId))
                    .collect(Collectors.toList());
        }

        public int getMemberCount(UUID teamId) {
            return findById(teamId)
                    .map(Team::getMemberCount)
                    .orElse(0);
        }

        /** Resolves memberIds to full User objects for a given team. */
        public List<User> getMembers(UUID teamId) {
            return findById(teamId)
                    .map(t -> userRepository.findAllByIds(t.getMemberIds()))
                    .orElse(Collections.emptyList());
        }

        /** Returns the leader User object for a given team. */
        public Optional<User> getLeader(UUID teamId) {
            return findById(teamId)
                    .flatMap(t -> userRepository.findById(t.getLeaderId()));
        }

        public boolean isMember(UUID teamId, UUID userId) {
            return findById(teamId)
                    .map(t -> t.hasMember(userId))
                    .orElse(false);
        }

        public boolean isLeader(UUID teamId, UUID userId) {
            return findById(teamId)
                    .map(t -> t.isLeader(userId))
                    .orElse(false);
        }

        // ── Member mutations ──────────────────────────────────────────────────────

        public void addMember(UUID teamId, UUID userId) {
            findById(teamId).ifPresent(team -> {
                team.addMember(userId);
                save(team);
            });
        }

        public void removeMember(UUID teamId, UUID userId) {
            findById(teamId).ifPresent(team -> {
                team.removeMember(userId);
                save(team);
            });
        }
    }