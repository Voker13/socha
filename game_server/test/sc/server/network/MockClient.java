package sc.server.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.protocol.responses.RoomPacket;
import sc.server.Configuration;
import sc.server.helpers.StringNetworkInterface;

import com.thoughtworks.xstream.XStream;

public class MockClient extends Client
{
	private final static Logger	logger				= LoggerFactory
															.getLogger(MockClient.class);
	private final Queue<Object>	outgoingMessages	= new LinkedList<Object>();
	private Object				object				= null;
	private final XStream		xStream;

	public MockClient(StringNetworkInterface stringInterface, XStream xStream)
			throws IOException
	{
		super(stringInterface, xStream);
		this.xStream = xStream;
	}

	public MockClient() throws IOException
	{
		this(new StringNetworkInterface("<protocol>"), Configuration
				.getXStream());
	}

	@Override
	public synchronized void send(Object packet)
	{
		super.send(packet);

		Object parsedPacket = this.xStream.fromXML(this.xStream.toXML(packet));
		this.outgoingMessages.add(parsedPacket);
	}

	public Object popMessage()
	{
		return this.outgoingMessages.poll();
	}

	@SuppressWarnings("unchecked")
	public <T> T seekMessage(Class<T> type)
	{
		int i = -1;
		Object current = null;
		do
		{
			i++;
			current = popMessage();
		} while (current != null && current.getClass() != type);

		if (current == null)
		{
			throw new RuntimeException(
					"Could not find a message of the specified type");
		}
		else
		{
			if (i > 0)
			{
				logger.info("Skipped {} messages.", i);
			}
			return (T) current;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T seekRoomMessage(String roomId, Class<T> type)
	{
		int i = -1;
		Object current = null;
		do
		{
			i++;
			RoomPacket response = seekMessage(RoomPacket.class);
			if (roomId.equals(response.getRoomId()))
			{
				current = response.getData();
			}
		} while (current != null && current.getClass() != type);

		if (current == null)
		{
			throw new RuntimeException(
					"Could not find a message of the specified type");
		}
		else
		{
			if (i > 0)
			{
				logger.info("Skipped {} messages.", i);
			}
			return (T) current;
		}
	}

	@Override
	protected void onObject(Object o)
	{
		super.onObject(o);
		this.object = o;
	}

	public Object receive() throws InterruptedException
	{
		while (this.object == null)
		{
			Thread.sleep(10);
		}

		return this.object;
	}
}
