import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Properties;
import java.util.Arrays;
import java.util.logging.Level;

import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;

public class TS3PresenceEngine implements Propertied {

    private SlackSession session;
	private HashMap<Integer,PresenceTuple> presenceState;
    private SlackChannel channel;
    private boolean onSwitch;
    private boolean squelch;
    public String propertiesName;
	private Properties props;
    private ArrayList<String> ignoreList;
    private TS3Query query;
    private TS3Api api;

    public TS3PresenceEngine(SlackSession session, SlackChannel channel) {
        this.session = session;
		presenceState = new HashMap<Integer,PresenceTuple>();
        this.channel = channel;
		propertiesName = this.getClass().getSimpleName();
		props = loadPropertiesFromDisk(propertiesName);
        onSwitch = false;
        squelch = true;
        query = null;
        api = null;
        initIgnores();
    }
	
	public Properties seedProperties() {
		Properties props = new Properties();
		props.setProperty("ignore_list","blah");
		props.setProperty("ts3_user","blah");
		props.setProperty("ts3_pass","blah");
		props.setProperty("ts3_server","blah");
		props.setProperty("botnick","blah");
		return props;
	}
	
	public Properties getProperties() {
		return props;
	}


    public HashMap<Integer,PresenceTuple> getPresenceState() {
        return presenceState;
    }

    public ArrayList<String> getIgnoreList() {
        return ignoreList;
    }

    public void purgeIgnoreList() {
        ignoreList = new ArrayList<String>();
    }

    private void initIgnores() {
        String rawIgnores = props.getProperty("ignore_list");
        if (rawIgnores != null && !rawIgnores.isEmpty()) {
            List<String> temptlist  = Arrays.asList(rawIgnores.split("\\s*,\\s*"));
            ignoreList = new ArrayList<String>(temptlist);
        } else {
            ignoreList = new ArrayList<String>();
        }
    }

    public List<String> getIgnores() {
        return ignoreList;
    }

    public boolean shouldIgnore(String input) {
        String nickname = input.toLowerCase();
        for (String toMatch : ignoreList) {
            if (toMatch.endsWith("*")) {
                if (nickname.startsWith(toMatch.substring(0,
                    toMatch.length()-1))) {
                    return true;
                }
            } else {
                if (toMatch.equals(nickname)) {
                    return true;
                }
            }
        }
        return false;
    }


    public void turnOn() {
        if (!onSwitch) {
            this.connect();
        }
        onSwitch = true;
    }

    public void squelchOn() {
        squelch = true;
    }

    public void squelchOff() {
        squelch = false;
    }

    public void turnOff() {
        if (onSwitch) {
            this.disconnect();
        }
        onSwitch = false;
    }

    public Boolean isOn() {
        return onSwitch;
    }

    public void clientJoin(int clientID, String nickname, int channelID) {
        //System.out.println("CLIENT JOIN: " + clientID + " " + nickname + " " + channelID);
        String newChannel = this.api.getChannelInfo(channelID).getName();
        presenceState.put(clientID, new PresenceTuple(nickname, newChannel));
        if (!this.shouldIgnore(nickname)) {
        	session.sendMessage(channel, "["+nickname+"] has JOINED Channel "+newChannel+".", null);
        }
    }

    public void clientMoved(int clientID, int channelID) {
        //System.out.println("CLIENT MOVE: " + clientID + " " + channelID);
        String nickname = "";
        String newChannel = this.api.getChannelInfo(channelID).getName();
        if (presenceState.containsKey(clientID)) {
            PresenceTuple ps = presenceState.get(clientID);
            nickname = ps.nickname;
            String oldChannel = ps.channel;
            if (newChannel.equals(oldChannel)) {
                return;
            }
        } else {
            return;
        }
        presenceState.put(clientID, new PresenceTuple(nickname, newChannel));
        if (!this.shouldIgnore(nickname)) {
            session.sendMessage(channel, "["+nickname+"] has MOVED to Channel "+newChannel+".", null);
        }
    }

    public void clientLeft(int clientID) {
        //System.out.println("CLIENT LEAVE: " + clientID);
        String nickname = "";
        if (presenceState.containsKey(clientID)) {
            nickname = presenceState.get(clientID).nickname;
        }
        presenceState.remove(nickname);
        if (!this.shouldIgnore(nickname)) {
            session.sendMessage(channel, "["+nickname+"] has QUIT Teamspeak.",null);
        }
    }

    public void refreshClients() {
        this.presenceState.clear();
        for (Client c : api.getClients()) {
            presenceState.put(c.getId(), new PresenceTuple(c.getNickname(),
                api.getChannelInfo(c.getChannelId()).getName()));
        }
    }

    public void connect() {
        String ts3User = props.getProperty("ts3_user");
        String ts3Pass = props.getProperty("ts3_pass");
        String ts3Server = props.getProperty("ts3_server");
        String botNick = props.getProperty("botnick");

        if (ts3User.equals("") || ts3Pass.equals("") || ts3Server.equals("")) {
            throw new IllegalArgumentException("Missing user, pass, or server property.");
        }

        TS3Config config = new TS3Config();
        config.setHost(ts3Server);
        config.setDebugLevel(Level.WARNING);
        config.setLoginCredentials(ts3User, ts3Pass);

        this.query = new TS3Query(config);
        this.query.connect();

        this.api = query.getApi();
        api.selectVirtualServerById(1);
        api.setNickname(botNick);

        //Populate initial state
        this.refreshClients();

        api.registerEvent(TS3EventType.CHANNEL, 0);
        api.addTS3Listeners(new TS3ListenerImpl(this));
    }

    public void disconnect() {
        this.api.quit();
        this.query.exit();
        this.api = null;
        this.query = null;
        this.presenceState.clear();
    }
}

