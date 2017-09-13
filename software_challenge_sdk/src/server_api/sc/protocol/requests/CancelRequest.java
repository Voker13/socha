package sc.protocol.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import sc.protocol.responses.ProtocolMessage;

@XStreamAlias("cancel")
public class CancelRequest extends ProtocolMessage implements ILobbyRequest
{
	@XStreamAsAttribute
	public String	roomId;

        /**
         * might be needed by XStream
         */
        public CancelRequest() {
        }

	public CancelRequest(String roomId)
	{
		this.roomId = roomId;
	}
}
