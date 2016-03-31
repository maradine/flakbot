import java.util.Properties;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import java.util.Scanner;

class GeneralHandler implements SlackMessagePostedListener, Propertied {

	private String userName;
	private Properties props;
	private String propertiesName;

	public GeneralHandler(String userName) {
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
		//System.out.println("MESSAGE RECEIVED: " + content);
		String command = content.toLowerCase();
		Scanner scanner = new Scanner(command);
        String token = scanner.next();

        if (token.equals("!shutdown")) {
			//do we want to implement this?
			
			//permissions check
		} else if(token.equals("!reload")) {
			//do we want to implement this?
			
			//permissions check
	
		} else if(token.equals("!topic")) {
            session.sendMessage(event.getChannel(), "The topic is: " + event.getChannel().getTopic(), null);

		}
	}


}
