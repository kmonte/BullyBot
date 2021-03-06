package core.entities;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import core.util.Utils;
import core.Constants;
import core.Database;
import core.util.Trigger;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

// Server class; Controls bot actions for each server
public class Server {
	private String id;
	private Guild guild;
	private Settings settings;
	private QueueManager qm;
	private HashMap<User, Long> activityList = new HashMap<User, Long>();
	private List<String> banList;
	private List<String> adminList;
	
	public Commands cmds;

	public Server(Guild guild) {
		this.guild = guild;
		this.id = guild.getId();
		Utils.createDir(String.format("%s/%s", "app_data", id));
		qm = new QueueManager(id);
		cmds = new Commands();
		settings = new Settings(id);
		qm.getQueueList().forEach((q) -> q.getPlayersInQueue().forEach((u) -> updateActivityList(u)));
		
		// Insert guild into database
		Database.insertDiscordServer(guild.getIdLong(), guild.getName());
		// Insert members into database
		for(Member m : guild.getMembers()){
			Database.insertPlayer(m.getUser().getIdLong(), m.getEffectiveName());
			Database.insertPlayerServer(guild.getIdLong(), m.getUser().getIdLong());
		}
		
		banList = Database.queryGetBanList(guild.getIdLong());
		adminList = Database.queryGetAdminList(guild.getIdLong());
		
		startAFKTimer();
	}

	public String getid() {
		return id;
	}

	public QueueManager getQueueManager() {
		return qm;
	}

	public Settings getSettings() {
		return settings;
	}

	public TextChannel getPugChannel() {
		for (TextChannel c : guild.getTextChannels()) {
			if (c.getName().equalsIgnoreCase(settings.pugChannel())) {
				return c;
			}
		}
		return guild.getDefaultChannel();
	}
	
	private void startAFKTimer() {
		Trigger tt = () -> afkTimerEnd();
		Timer afkTimer = new Timer(60, tt);
		afkTimer.start();
	}

	private void afkTimerEnd() {
		activityList.forEach((u, l) -> {
			if (qm.isPlayerInQueue(u) && (System.currentTimeMillis() - l) / 60000 >= settings.afkTime()) {
				qm.deletePlayer(u);
				String s = String.format("%s has been removed from the queue due to inactivity", u.getName());
				getPugChannel().sendMessage(Utils.createMessage("", s, Color.red)).queue();
				u.openPrivateChannel().complete().sendMessage("You have been removed from the queue due to inactivity")
						.queue();
				qm.updateTopic();
				System.out.println(s);
			}
		});
		startAFKTimer();
	}

	private void startDcTimer(Member m) {
		Trigger trigger = () -> dcTimerEnd(m);
		Timer timer = new Timer(settings.dcTime(), trigger);
		timer.start();
		System.out.println(String.format("User %s has gone offline, starting dc timer", m.getEffectiveName()));
	}

	private void dcTimerEnd(Member m) {
		if (m.getOnlineStatus().equals(OnlineStatus.OFFLINE) && qm.isPlayerInQueue(m.getUser())) {
			qm.deletePlayer(m.getUser());
			qm.updateTopic();
			String s = String.format("%s has been removed from queue after being offline for %s minutes", m.getEffectiveName(), new DecimalFormat("#.##").format((double)settings.dcTime()/60));
			getPugChannel().sendMessage(Utils.createMessage("", s,Color.red)).queue();
			System.out.println(s);
		}
	}
	
	public void updateActivityList(User u){
		if(qm.isPlayerInQueue(u) || qm.hasPlayerJustFinished(u)){
			activityList.put(u, System.currentTimeMillis());
		}else if(activityList.containsKey(u)){
			activityList.remove(u);
		}
	}
	
	public void playerDisconnect(Member m){
		if(qm.isPlayerInQueue(m.getUser()) || qm.hasPlayerJustFinished(m.getUser())){
			startDcTimer(m);
		}
	}
	
	public Guild getGuild(){
		return guild;
	}
	
	public void setGuild(Guild guild){
		this.guild = guild;
	}
	
	public boolean isAdmin(Member m){
		if(adminList.contains(m.getUser().getId()) 
				|| m.hasPermission(Permission.KICK_MEMBERS) 
				|| m.getUser().getId().equals(Constants.OWNER_ID)){
			return true;
		}
		return false;
	}
	
	public boolean isBanned(Member m){
		return banList.contains(m.getUser().getId());
	}
	
	public Member getMember(String player){
		for(Member m : guild.getMembers()){
			if(m.getEffectiveName().equalsIgnoreCase(player) || m.getUser().getId().equals(player)){
				return m;
			}
		}
		return null;
	}
	
	public void banUser(String playerId){
		if(!banList.contains(playerId)){
			banList.add(playerId);
		}
		Database.updateBanStatus(guild.getIdLong(), Long.valueOf(playerId), true);
	}
	
	public void unbanUser(String playerId){
		banList.remove(playerId);
		Database.updateBanStatus(guild.getIdLong(), Long.valueOf(playerId), false);
	}
	
	public void unbanAll(){
		for(String s : banList){
			Database.updateBanStatus(guild.getIdLong(), Long.valueOf(s), false);
		}
		banList.clear();
	}
	
	public void addAdmin(String playerId){
		if(!adminList.contains(playerId)){
			adminList.add(playerId);
		}
		Database.updateAdminStatus(guild.getIdLong(), Long.valueOf(playerId), true);
	}
	
	public void removeAdmin(String playerId){
		adminList.remove(playerId);
		Database.updateAdminStatus(guild.getIdLong(), Long.valueOf(playerId), false);
	}
}
