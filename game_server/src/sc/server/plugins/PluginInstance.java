package sc.server.plugins;

import sc.api.plugins.IPlugin;
import sc.api.plugins.PluginDescriptor;

public class PluginInstance<HostType, PluginType extends IPlugin<HostType>>
{
	private final Class<?>			definition;
	private PluginType				instance;
	private final PluginDescriptor	description;

	public PluginInstance(Class<?> definition)
	{
		this.definition = definition;
		this.description = definition.getAnnotation(PluginDescriptor.class);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends PluginType> uncheckedDefinitionCast(
			Class<?> definition) throws ClassCastException
	{
		return (Class<? extends PluginType>) definition;
	}

	public PluginType getPlugin()
	{
		return instance;
	}

	public PluginDescriptor getDescription()
	{
		return description;
	}

	public void load(HostType host) throws PluginLoaderException
	{
		instanciate();
		this.instance.initialize(host);
	}

	public void unload()
	{
		this.instance.unload();
	}

	private void instanciate() throws PluginLoaderException
	{
		try
		{
			Class<? extends PluginType> castedDefintion = uncheckedDefinitionCast(definition);
			this.instance = castedDefintion.newInstance();
		}
		catch (IllegalAccessException e)
		{
			throw new PluginLoaderException(e);
		}
		catch (InstantiationException e)
		{
			throw new PluginLoaderException(e);
		}
		catch (ClassCastException e)
		{
			throw new PluginLoaderException(e);
		}
	}
}
