import java.util.Properties;
import java.io.IOException;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.util.Collection;

public class FlakBot {

	public static void main(String[] args) {

		Properties props = null;
		try {
			props = PropertiesManager.loadPropertiesFromDisk("FlakBot");
		} catch (IOException ioe) {
			System.out.println("Failed to load properties at launch.");
			System.exit(1);
		}

		String slackApiKey = props.getProperty("slack_api_key");
		final SlackSession session = SlackSessionFactory.createWebSocketSlackSession(slackApiKey);
		
		try {
			session.connect();
		} catch (IOException ioe) {
			System.out.println("connection to slack failed.");
			System.out.println(ioe);
			System.exit(1);
		}

		String userName = session.sessionPersona().getUserName();
		String channelName = "fkpk";
		SlackChannel channel = session.findChannelByName(channelName);
		
		
		session.addMessagePostedListener(new BanterBox(userName));
    	session.addMessagePostedListener(new GeneralHandler(userName));
		
		
		TS3PresenceEngine pe = new TS3PresenceEngine(session, channel);
		session.addMessagePostedListener(new TS3PresenceHandler(pe));

		TwitchPresenceEngine tpe = new TwitchPresenceEngine(session, channel);
		Thread tpt = new Thread(tpe, "tpt");
		tpt.start();
		session.addMessagePostedListener(new TwitchPresenceHandler(tpe));

		while (true){
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("I caught a main loop exception which makes no fuckign sense.");
				System.out.println(e);
			}
    	}
	}

}
