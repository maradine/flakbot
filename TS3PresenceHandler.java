import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.util.Properties;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import java.util.logging.Level;
import java.io.FileOutputStream;
import java.io.IOException;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;


public class TS3PresenceHandler implements SlackMessagePostedListener {


    private Scanner scanner;
    private TS3PresenceEngine pe;
    private Properties props;
    private Date lastRefresh;

    public TS3PresenceHandler(TS3PresenceEngine pe) {
        super();
        this.pe = pe;
        this.props = pe.getProperties();
        this.lastRefresh = new Date();
    }
	public void onEvent(SlackMessagePosted event, SlackSession session){

		String content = event.getMessageContent();
		String command = content.toLowerCase();
		Scanner scanner = new Scanner(command);
        String token = scanner.next();


        if (command.startsWith("!presence ")) {

            if (scanner.hasNext()){
                token = scanner.next().toLowerCase();
                switch (token) {
                    //
                    case "ignore": if (!scanner.hasNext()){
                        session.sendMessage(event.getChannel(),"List, add, remove, purge, save.",null);
                    } else {
                        token = scanner.next().toLowerCase();
                        if (token.equals("list")) {
                            session.sendMessage(event.getChannel(),"Current ignored nicknames: "+pe.getIgnoreList(),null);
                        } else if (token.equals("add")) {
                            if (!scanner.hasNext()) {
                                session.sendMessage(event.getChannel(),"Who am I adding?",null);
                            } else {
                                token = scanner.next().toLowerCase();
                                ArrayList<String> list = pe.getIgnoreList();
                                if (list.contains(token)) {
                                    session.sendMessage(event.getChannel(),"Ignore list already contains "+ token +".",null);
                                } else {
                                    list.add(token);
                                    session.sendMessage(event.getChannel(),"Added "+token+" to the ignore list.",null);
                                }
                            }
                        } else if (token.equals("remove")) {
                            if (!scanner.hasNext()) {
                                session.sendMessage(event.getChannel(),"Who am I removing?",null);
                            } else {
                                token = scanner.next().toLowerCase();
                                List<String> list = pe.getIgnoreList();
                                if (!list.contains(token)) {
                                    session.sendMessage(event.getChannel(),"Ignore list does not contain "+ token +".",null);
                                } else {
                                    list.remove(token);
                                    session.sendMessage(event.getChannel(),"Removed "+token+" from the ignore list.",null);
                                }
                            }
                        } else if (token.equals("purge")) {
                            pe.purgeIgnoreList();
                            session.sendMessage(event.getChannel(),"Ignore list purged.",null);
                        } else if (token.equals("save")) {
                            String saver = "";
                            for (String s : pe.getIgnoreList()) {
                                saver = saver + s + ",";
                            }
                            if (saver.length()>0) {
                                saver = saver.substring(0, saver.length()-1);
                            }
                            props.setProperty("ignore_list", saver);
                            pe.savePropertiesToDisk(pe.propertiesName, props);
                            session.sendMessage(event.getChannel(),"Ignore list saved.",null);
                        } else {
                            session.sendMessage(event.getChannel(),"List, add, remove, purge, save.",null);
                        }
                    }

                    break;
                    //
                    //
                    case "on": pe.turnOn();
                    session.sendMessage(event.getChannel(),"Auto-presence turned ON.",null);
                    break;
                    //
                    case "off": pe.turnOff();
                    session.sendMessage(event.getChannel(),"Auto-presence turned OFF.",null);
                    break;
                    //
                    default: session.sendMessage(event.getChannel(),"I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"ignore\", \"timeout\", and \"interval\".",null);
                    break;

                }
            }
        }
        if (command.equals("!presence")) {
            if (pe.isOn()) {
                Date now = new Date();
                //Force a refresh up to every minute due to API thread lols
                if (now.getTime() - this.lastRefresh.getTime() > 60000) {
                    pe.refreshClients();
                    this.lastRefresh = now;
                }
                String toPrint = "";
                ArrayList<String> ignores = pe.getIgnoreList();
                for (PresenceTuple pt : pe.getPresenceState().values()) {
                    if (!pe.shouldIgnore(pt.nickname)) {
                        if (!toPrint.equals("")) {
                            toPrint += ", ";
                        }
                        toPrint += pt.nickname + "=" + pt.channel;
                    }
                }
                session.sendMessage(event.getChannel(),"Users: " + toPrint,null);
            } else {
                session.sendMessage(event.getChannel(),"Presence engine wasn't started, so I have no state to report.",null);
            }

        }

    }
}

