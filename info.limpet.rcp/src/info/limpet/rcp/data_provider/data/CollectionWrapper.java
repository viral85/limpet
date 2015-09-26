package info.limpet.rcp.data_provider.data;

import info.limpet.ICollection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

public class CollectionWrapper implements IAdaptable, LimpetWrapper
{
	private final ICollection _collection;
	private final LimpetWrapper _parent;

	public CollectionWrapper(final LimpetWrapper parent,
			final ICollection collection)
	{
		_parent = parent;
		_collection = collection;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter)
	{
		if (adapter == IPropertySource.class)
		{
			return new CollectionPropertySource(this);
		}
		else if (adapter == ICollection.class)
		{
			return _collection;
		}
		return null;
	}

	public ICollection getCollection()
	{
		return _collection;
	}

	@Override
	public LimpetWrapper getParent()
	{
		return _parent;
	}

	@Override
	public Object getSubject()
	{
		return _collection;
	}

	@Override
	public String toString()
	{
		return _collection.getName() + " (" + _collection.size() + " items)";
	}
}