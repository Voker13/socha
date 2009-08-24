package sc.server.network;

import java.io.IOException;
import junit.framework.Assert;

import org.junit.Test;

import sc.helpers.Generator;
import sc.networking.clients.LobbyClient;
import sc.protocol.helpers.RequestResult;
import sc.protocol.responses.PrepareGameResponse;
import sc.server.Configuration;
import sc.server.helpers.TestHelper;
import sc.server.plugins.TestPlugin;

public class SampleLibraryTest extends RealServerTest
{
	static class TestLobbyClient
	{
		private LobbyClient	client;

		public TestLobbyClient(String gameType, String host, int port)
				throws IOException
		{
			this.client = new LobbyClient(Configuration.getXStream(), null, host,
					port);
			this.client.start();
		}

		public LobbyClient getClient()
		{
			return this.client;
		}
	}

	@Test
	public void shouldConnectToServer() throws IOException
	{
		final TestLobbyClient client = new TestLobbyClient(
				TestPlugin.TEST_PLUGIN_UUID, "localhost", getServerPort());

		client.getClient().joinAnyGame(TestPlugin.TEST_PLUGIN_UUID);

		TestHelper.assertEqualsWithTimeout(1, new Generator<Integer>() {
			@Override
			public Integer operate()
			{
				return client.client.getRooms().size();
			}
		});
	}

	@Test
	public void shouldBeAbleToPlayTheGame() throws IOException
	{
		final TestLobbyClient client = new TestLobbyClient(
				TestPlugin.TEST_PLUGIN_UUID, "localhost", getServerPort());

		client.getClient().joinAnyGame(TestPlugin.TEST_PLUGIN_UUID);

		TestHelper.assertEqualsWithTimeout(1, new Generator<Integer>() {
			@Override
			public Integer operate()
			{
				return client.getClient().getRooms().size();
			}
		});
	}

	@Test
	public void shouldSupportBlockingHandlers() throws IOException,
			InterruptedException
	{
		final TestLobbyClient client = new TestLobbyClient(
				TestPlugin.TEST_PLUGIN_UUID, "localhost", getServerPort());

		RequestResult<PrepareGameResponse> result = client.client
				.prepareGameAndWait(TestPlugin.TEST_PLUGIN_UUID, 2);

		Assert.assertTrue(result.hasValidContents());
		Assert.assertTrue(result.isSuccessful());
	}
}
