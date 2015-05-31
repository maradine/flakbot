import java.util.Properties;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import java.util.Scanner;

class BanterBox implements SlackMessagePostedListener, Propertied {

	private String userName;
	private Properties props;
	private String propertiesName;

	public BanterBox(String userName) {
		super();
		this.userName = userName;
		propertiesName = this.getClass().getSimpleName();
		props = loadPropertiesFromDisk(propertiesName);
	}

	public Properties seedProperties() {
		Properties props = new Properties();
		//props.setProperty("","");
		return props;
	}
	
	public Properties getProperties() {
		return props;
	}
	

	public void onEvent(SlackMessagePosted event, SlackSession session){


		//for tis handler, ignore ourselves
		String sender = event.getSender().getUserName();
		if (userName.equals(sender)){
			//System.out.println("IGNORING MYSELF");
			return;
		}

		String content = event.getMessageContent();
		String command = content.toLowerCase();
		Scanner scanner = new Scanner(command);
        String token = scanner.next();

		if (command.startsWith("!presents ") || command.equals("!presents")) {
            session.sendMessage(event.getChannel(), "(ノಠ益ಠ)ノ彡┻━┻", null);

        } else if (command.startsWith("!preddance ") || command.equals("!preddance") || command.startsWith("!preddence ") || command.equals("!preddence ")) {
            session.sendMessage(event.getChannel(), "THIS IS A POEM ABOUT PREDD:\n\ni never attend /\nyour collective labors bore /\nautumn in the bronx", null);

        } else if (command.contains("nuestra") || command.contains("fiesta")){
            session.sendMessage(event.getChannel(), "ESTA ES NUESTRA FIESTA", null);

        } else if (command.contains("gabba")) {
            session.sendMessage(event.getChannel(), "GABBA GABBA GABBA HEY", null);

        } else if (command.contains("on fire")) {
            session.sendMessage(event.getChannel(), "SIR YOU ARE ON FIRE", null);
        }

	}

}
