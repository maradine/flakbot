import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.util.Properties;
import java.util.Arrays;
import java.util.logging.Level;

import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.events.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import com.mb3364.twitch.api.models.Stream;
import com.mb3364.twitch.api.models.Channel;
import com.mb3364.twitch.api.handlers.StreamsResponseHandler;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.http.RequestParams;

public class TwitchPresenceEngine implements Propertied,Runnable {

    private SlackSession session;
	private List<Stream> activeStreams;
	private List<String> followList;
    private SlackChannel channel;
    private boolean onSwitch;
    public String propertiesName;
	private Properties props;
	private long interval;
	private long backoff;
	private Twitch twitch;

    public TwitchPresenceEngine(SlackSession session, SlackChannel channel) {
        this.session = session;
		activeStreams = new LinkedList<Stream>();
		followList = new LinkedList<String>();
        this.channel = channel;
		propertiesName = this.getClass().getSimpleName();
		props = loadPropertiesFromDisk(propertiesName);
        onSwitch = true;
		interval = 60000L;
		backoff = 0L;
        initTracks();
		twitch = new Twitch();
    }
	
	public Properties seedProperties() {
		Properties props = new Properties();
		props.setProperty("follow_list","maradine,callofduty,riotgames");
		props.setProperty("botnick","blah");
		return props;
	}
	
	public Properties getProperties() {
		return props;
	}

    public List<String> getFollowList() {
        return followList;
    }

    public void purgeFollowList() {
        followList = new LinkedList<String>();
    }

    private void initTracks() {
        String rawFollows = props.getProperty("follow_list");
        if (rawFollows != null && !rawFollows.isEmpty()) {
            List<String> temptlist  = Arrays.asList(rawFollows.split("\\s*,\\s*"));
            followList = new LinkedList<String>(temptlist);
        } else {
            followList = new LinkedList<String>();
        }
    }

    public List<String> getTracks() {
        return followList;
    }

	public void setInterval(long set) {
		interval = set;
	}

	public long getInterval() {
		return interval;
	}

    public void turnOn() {
        onSwitch = true;
    }

    public void turnOff() {
        onSwitch = false;
    }

    public Boolean isOn() {
        return onSwitch;
    }

	public void run() {
		while (true) {
			try {
				Thread.sleep(interval);
				if (onSwitch) {
	

					RequestParams params = new RequestParams();
					String expandedFollows = "";
					for (String s : followList) {
						expandedFollows += (s+",");
					}
					if (expandedFollows.length() != 0) {
						expandedFollows = expandedFollows.substring(0,expandedFollows.length()-1);
					}

					params.put("channel", expandedFollows);
		
					twitch.streams().get(params, new StreamsResponseHandler() {
						@Override
						public void onSuccess(int total, List<Stream> newStreams) {
							//check new streams for presence in old streams, inate object equality via stream id.
							//if it's not present in the old list, report it.
							for (Stream ns : newStreams) {
								boolean sayit = true;
								for (Stream as : activeStreams) {
									if (as.equals(ns)) {
										sayit = false;
									}
								}
								if (sayit) {
									Channel c = ns.getChannel();
									String twitchName = c.getName();
									String status = c.getStatus();
									String url = c.getUrl();
									session.sendMessage(channel, twitchName + " is now streaming \""+status+"\" from " + url, null);
								}
							}
							activeStreams = newStreams;
							
						/*	System.out.println("I have "+total+" streams in the query");
							for (Stream s : streams) {
								//System.out.println(s);
								Channel c = s.getChannel();
								String twitchName = c.getName();
								String status = c.getStatus();
								String url = c.getUrl();
								System.out.println(twitchName + " is streaming \""+status+"\" from " + url);
							}
						*/
						
						
						
						}

						@Override
						public void onFailure(int statusCode, String statusMessage, String errorMessage) {
							System.out.println("Something went wrong but in the confines of the http return: "+statusMessage);
						}

						@Override
						public void onFailure(Throwable e) {
							System.out.println("Unhandled Exception; shutting down: "+e);
							System.exit(1);
						}
					});

				}

			} catch (InterruptedException e) {
				//bot.sendMessage(channel, "Interval timer interrupted - resetting backoff and restarting clock.");
/*			} catch (IOException e) {
				if (backoff==0L) {
					backoff = 300000L;
					//bot.sendMessage(channel, "Twitch just choked over an update check - sorry!  Backing off a bit.");
					System.out.println("API Failure - backoff is now "+backoff);
				} else if (backoff > 3600000L) {
					//bot.sendMessage(channel, "Enough API calls have failed that I'm shutting down the presence engine.  Please contact my owner.");
					System.out.println("API Failure");
					System.out.println("Shutting down presence and resetting timeouts.");
					this.turnOff();
					backoff=0L;
				} else {
					backoff = backoff*2;
					//bot.sendMessage(channel, "Twitch choked again.  Backing off further.");
					System.out.println("API Failure - backoff is now "+backoff);
				}
*/			}
		}
	}



}

