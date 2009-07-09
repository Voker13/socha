package sc.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.api.plugins.IPlayer;
import sc.api.plugins.exceptions.RescueableClientException;
import sc.protocol.RoomPacket;
import sc.protocol.requests.AuthenticateRequest;
import sc.protocol.requests.ILobbyRequest;
import sc.protocol.requests.JoinPreparedRoomRequest;
import sc.protocol.requests.JoinRoomRequest;
import sc.protocol.requests.PrepareGameRequest;
import sc.server.gaming.GameRoom;
import sc.server.gaming.GameRoomManager;
import sc.server.gaming.ReservationManager;
import sc.server.network.Client;
import sc.server.network.ClientManager;
import sc.server.network.IClientListener;
import sc.server.network.PacketCallback;

/**
 * The lobby will help clients find a open game or create new games to play with
 * another client.
 * 
 * @author mja
 * @author rra
 */
public class Lobby implements IClientManagerListener, IClientListener
{
	private Logger					logger			= LoggerFactory
															.getLogger(Lobby.class);
	private Map<IPlayer, Client>	players			= new HashMap<IPlayer, Client>();
	private GameRoomManager			gameManager		= new GameRoomManager();
	private ClientManager			clientManager	= new ClientManager();

	public Lobby()
	{
		clientManager.addListener(this);
	}

	public void start()
	{
		gameManager.start();
		clientManager.start();
	}

	@Override
	public void onClientConnected(Client client)
	{
		client.addClientListener(this);
	}

	@Override
	public void onClientDisconnected(Client source)
	{
		logger.info("{} disconnected.", source);
	}

	@Override
	public void onRequest(Client source, PacketCallback callback)
			throws RescueableClientException
	{
		Object packet = callback.getPacket();
		
		if (packet instanceof ILobbyRequest)
		{
			if (packet instanceof JoinPreparedRoomRequest)
			{
				ReservationManager
						.claimReservation(source,
								((JoinPreparedRoomRequest) packet)
										.getReservationCode());
			}
			else if (packet instanceof JoinRoomRequest)
			{
				gameManager.joinOrCreateGame(source, ((JoinRoomRequest) packet)
						.getGameType());
			}
			else if (packet instanceof AuthenticateRequest)
			{
				source.authenticate(((AuthenticateRequest) packet)
						.getPassword());
			}
			else if (packet instanceof PrepareGameRequest)
			{
				PrepareGameRequest prepared = (PrepareGameRequest) packet;
				source.send(gameManager.prepareGame(prepared.getGameType(),
						prepared.getPlayerCount()));
			}
			else if (packet instanceof RoomPacket)
			{
				RoomPacket casted = (RoomPacket) packet;
				GameRoom room = gameManager.findRoom(casted
						.getRoomId());
				room.onEvent(source, casted.getData());

			}
			else
			{
				throw new RescueableClientException(
						"Unhandled Packet of type: " + packet.getClass());
			}
			
			callback.setProcessed();
		}
	}

	public void close()
	{
		clientManager.close();
		gameManager.close();
	}

	public GameRoomManager getGameManager()
	{
		return this.gameManager;
	}

	public ClientManager getClientManager()
	{
		return this.clientManager;
	}
}
