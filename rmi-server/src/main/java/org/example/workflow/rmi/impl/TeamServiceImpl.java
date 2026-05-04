package org.example.workflow.rmi.impl;

import org.example.workflow.db.repository.TeamRepository;
import org.example.workflow.db.repository.UserRepository;
import org.example.workflow.model.Team;
import org.example.workflow.model.User;
import org.example.workflow.rmi.interfaces.TeamService;
import org.example.workflow.util.JsonMapper;
import org.example.workflow.util.ValidationUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//Uses String IDs at the boundary, converts to UUID internally for repository calls

public class TeamServiceImpl extends UnicastRemoteObject implements TeamService {

    private static final Logger log = Logger.getLogger(TeamServiceImpl.class.getName());

    private final TeamRepository teamRepo = new TeamRepository();
    private final UserRepository userRepo = new UserRepository();

    public TeamServiceImpl() throws RemoteException { super(); }

    private UUID uid(String s) throws RemoteException {
        try { return UUID.fromString(s); }
        catch (Exception e) { throw new RemoteException("Invalid UUID: " + s); }
    }

    private User requireUser(String id) throws RemoteException {
        return userRepo.findById(uid(id))
                .orElseThrow(() -> new RemoteException("User not found: " + id));
    }

    private Team requireTeam(String id) throws RemoteException {
        return teamRepo.findById(uid(id))
                .orElseThrow(() -> new RemoteException("Team not found: " + id));
    }

    @Override
    public String createTeam(String requesterId, String name,
                             String description) throws RemoteException {
        User requester = requireUser(requesterId);

        // Only Leaders can create teams
        if (!requester.isLeader())
            throw new RemoteException("Only leaders can create teams.");

        // User must not already be in a team
        if (requester.getTeamId() != null)
            throw new RemoteException("You are already in a team. Disband first.");

        // Build team
        UUID   teamId     = UUID.randomUUID();
        String inviteCode = ValidationUtils.generateInviteCode();
        Team   team       = new Team(teamId, name, inviteCode, requester.getId());
        team.setDescription(description);
        teamRepo.save(team);

        // Update creators teamId
        requester.setTeamId(teamId);
        userRepo.save(requester);

        log.info("[RMI] Team created: " + team);
        return JsonMapper.toJson(team);
    }

    @Override
    public String joinTeam(String requesterId, String inviteCode) throws RemoteException {
        User requester = requireUser(requesterId);

        if (requester.getTeamId() != null)
            throw new RemoteException("You are already in a team.");

        Team team = teamRepo.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RemoteException("Invalid invite code."));

        team.addMember(requester.getId());
        teamRepo.save(team);

        requester.setTeamId(team.getId());
        userRepo.save(requester);

        log.info("[RMI] " + requester.getUsername() + " joined team " + team.getName());
        return JsonMapper.toJson(team);
    }

    @Override
    public String addMemberByUsername(String requesterId, String teamId,
                                      String username) throws RemoteException {
        requireUser(requesterId);
        UUID tId = uid(teamId);

        if (!teamRepo.isLeader(tId, uid(requesterId)))
            throw new RemoteException("Only the leader can add members.");

        User target = userRepo.findByUsername(username)
                .orElseThrow(() -> new RemoteException("User not found: " + username));

        if (target.getTeamId() != null)
            throw new RemoteException(username + " is already in a team.");

        teamRepo.addMember(tId, target.getId());
        target.setTeamId(tId);
        userRepo.save(target);

        log.info("[RMI] Member " + username + " added to team " + tId);
        return JsonMapper.toJson(target.toClientSafe());
    }

    @Override
    public boolean removeMember(String requesterId, String teamId,
                                String userId) throws RemoteException {
        requireUser(requesterId);
        UUID tId = uid(teamId);
        UUID uId = uid(userId);

        if (!teamRepo.isLeader(tId, uid(requesterId)))
            throw new RemoteException("Only the leader can remove members.");

        if (uId.equals(uid(requesterId)))
            throw new RemoteException("Leader cannot remove themselves. Use disbandTeam instead.");

        teamRepo.removeMember(tId, uId);
        userRepo.findById(uId).ifPresent(u -> {
            u.setTeamId(null);
            userRepo.save(u);
        });

        log.info("[RMI] Member " + userId + " removed from team " + teamId);
        return true;
    }

    @Override
    public String getTeamMembers(String requesterId, String teamId) throws RemoteException {
        requireUser(requesterId);
        UUID tId = uid(teamId);

        if (!teamRepo.isMember(tId, uid(requesterId)))
            throw new RemoteException("You are not a member of this team.");

        List<User> members = teamRepo.getMembers(tId).stream()
                .map(User::toClientSafe)
                .collect(Collectors.toList());
        return JsonMapper.toJson(members);
    }

    @Override
    public String getMyTeam(String requesterId) throws RemoteException {
        User requester = requireUser(requesterId);
        if (requester.getTeamId() == null)
            throw new RemoteException("You are not in a team.");
        Team team = requireTeam(requester.getTeamId().toString());
        return JsonMapper.toJson(team);
    }

    @Override
    public boolean disbandTeam(String requesterId, String teamId) throws RemoteException {
        requireUser(requesterId);
        UUID tId = uid(teamId);
        Team team = requireTeam(teamId);

        if (!team.getLeaderId().equals(uid(requesterId)))
            throw new RemoteException("Only the leader can disband the team.");

        // Clear teamId for all members
        for (UUID memberId : team.getMemberIds()) {
            userRepo.findById(memberId).ifPresent(u -> {
                u.setTeamId(null);
                userRepo.save(u);
            });
        }

        teamRepo.delete(tId);
        log.info("[RMI] Team disbanded: " + teamId);
        return true;
    }

    @Override
    public String getAvailableUsers(String requesterId) throws RemoteException {
        requireUser(requesterId);
        // All users with no team, excluding the requester
        UUID rId = uid(requesterId);
        List<User> available = userRepo.findAll().stream()
                .filter(u -> u.getTeamId() == null)
                .filter(u -> !u.getId().equals(rId))
                .map(User::toClientSafe)
                .collect(Collectors.toList());
        return JsonMapper.toJson(available);
    }

    @Override
    public String ping() throws RemoteException { return "pong — TeamService alive"; }
}