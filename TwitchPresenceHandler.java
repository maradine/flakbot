import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.io.FileOutputStream;
import java.io.IOException;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;


public class TwitchPresenceHandler implements SlackMessagePostedListener {


    private Scanner scanner;
    private TwitchPresenceEngine tpe;
    private Properties props;
    private Date lastRefresh;

    public TwitchPresenceHandler(TwitchPresenceEngine tpe) {
        super();
        this.tpe = tpe;
        this.props = tpe.getProperties();
        this.lastRefresh = new Date();
    }
	public void onEvent(SlackMessagePosted event, SlackSession session){

		String content = event.getMessageContent();
		String command = content.toLowerCase();
		Scanner scanner = new Scanner(command);
        String token = scanner.next();


        if (command.startsWith("!twitch ")) {

            if (scanner.hasNext()){
                token = scanner.next();
                switch (token) {
                    //
                    case "follow": if (!scanner.hasNext()){
                        session.sendMessage(event.getChannel(),"List, add, remove, purge, save.",null);
                    } else {
                        token = scanner.next().toLowerCase();
                        if (token.equals("list")) {
                            session.sendMessage(event.getChannel(),"Current followed twitch usernames: "+tpe.getFollowList(),null);
                        } else if (token.equals("add")) {
                            if (!scanner.hasNext()) {
                                session.sendMessage(event.getChannel(),"Who am I adding?",null);
                            } else {
                                token = scanner.next().toLowerCase();
                                List<String> list = tpe.getFollowList();
                                if (list.contains(token)) {
                                    session.sendMessage(event.getChannel(),"Follow list already contains "+ token +".",null);
                                } else {
                                    list.add(token);
                                    session.sendMessage(event.getChannel(),"Added "+token+" to the follow list.",null);
                                }
                            }
                        } else if (token.equals("remove")) {
                            if (!scanner.hasNext()) {
                                session.sendMessage(event.getChannel(),"Who am I removing?",null);
                            } else {
                                token = scanner.next().toLowerCase();
                                List<String> list = tpe.getFollowList();
                                if (!list.contains(token)) {
                                    session.sendMessage(event.getChannel(),"Follow list does not contain "+ token +".",null);
                                } else {
                                    list.remove(token);
                                    session.sendMessage(event.getChannel(),"Removed "+token+" from the follow list.",null);
                                }
                            }
                        } else if (token.equals("purge")) {
                            tpe.purgeFollowList();
                            session.sendMessage(event.getChannel(),"Follow list purged.",null);
                        } else if (token.equals("save")) {
                            String saver = "";
                            for (String s : tpe.getFollowList()) {
                                saver = saver + s + ",";
                            }
                            if (saver.length()>0) {
                                saver = saver.substring(0, saver.length()-1);
                            }
                            props.setProperty("follow_list", saver);
                            tpe.savePropertiesToDisk(tpe.propertiesName, props);
                            session.sendMessage(event.getChannel(),"Follow list saved.",null);
                        } else {
                            session.sendMessage(event.getChannel(),"List, add, remove, purge, save.",null);
                        }
                    }

                    break;
                    //
                    //
                    case "on": tpe.turnOn();
                    session.sendMessage(event.getChannel(),"Twitch following turned ON.",null);
                    break;
                    //
                    case "off": tpe.turnOff();
                    session.sendMessage(event.getChannel(),"Twitch following turned OFF.",null);
                    break;
                    //
                    default: session.sendMessage(event.getChannel(),"I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"follow\", and \"interval\".",null);
                    break;

                }
            }
        }
        if (command.equals("!twitch")) {
            if (tpe.isOn()) {
                session.sendMessage(event.getChannel(),"Use '!twitch follow' to see commands.  Maybe someday this will do something.",null);
            } else {
                session.sendMessage(event.getChannel(),"Twitch engine wasn't started, so I have no state to report.",null);
            }

        }

    }
}

