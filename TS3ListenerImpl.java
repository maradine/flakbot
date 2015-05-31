import com.github.theholywaffle.teamspeak3.api.event.*;

public class TS3ListenerImpl implements TS3Listener {
    private TS3PresenceEngine parent;

    public TS3ListenerImpl(TS3PresenceEngine tpe) {
        this.parent = tpe;
    }

    public void onClientJoin(ClientJoinEvent e) {
        this.parent.clientJoin(e.getClientId(), e.getClientNickname(), e.getClientTargetId());
    }

    public void onClientMoved(ClientMovedEvent e) {
        this.parent.clientMoved(e.getClientId(), e.getClientTargetId());
    }

    public void onClientLeave(ClientLeaveEvent e) {
        this.parent.clientLeft(e.getClientId());
    }

    public void onTextMessage(TextMessageEvent e) {}
    public void onServerEdit(ServerEditedEvent e) {}
    public void onChannelEdit(ChannelEditedEvent e) {}
    public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {}
	public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {}
	public void onChannelMoved(ChannelMovedEvent e) {}
	public void onChannelCreate(ChannelCreateEvent e) {}
	public void onChannelDeleted(ChannelDeletedEvent e) {}
}

