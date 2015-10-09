package info.limpet.data;

import info.limpet.ICommand;
import info.limpet.IStore;
import info.limpet.IStore.IStoreItem;
import info.limpet.data.impl.TemporalObjectCollection;
import info.limpet.data.impl.samples.StockTypes;
import info.limpet.data.impl.samples.StockTypes.NonTemporal.Location;
import info.limpet.data.impl.samples.StockTypes.Temporal;
import info.limpet.data.operations.location.DistanceBetweenTracksOperation;
import info.limpet.data.operations.location.GeoSupport;
import info.limpet.data.store.InMemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.factory.Hints;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.GeometryFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Assert;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Geometry;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.referencing.operation.TransformException;

public class TestGeotoolsGeometry extends TestCase
{

	public void testCreateTemporalObjectCollection()
	{
		TemporalObjectCollection<Geometry> locations = new TemporalObjectCollection<Geometry>(
				"test");
		assertNotNull(locations);
	}
	
	public void testBuilder() throws TransformException
	{
		final Location track_1 = new StockTypes.NonTemporal.Location("some location data");
		
		GeometryBuilder builder = new GeometryBuilder( DefaultGeographicCRS.WGS84 );
		GeodeticCalculator geoCalc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
		DirectPosition pos_1 = new DirectPosition2D(-4,  55.8);
		geoCalc.setStartingGeographicPoint(pos_1.getOrdinate(0), pos_1.getOrdinate(1));
		geoCalc.setDirection(Math.toRadians(54), 0.003);
		pos_1 = geoCalc.getDestinationPosition();
		
		Point p1 = builder.createPoint(pos_1.getOrdinate(0), pos_1.getOrdinate(1));
		track_1.add(p1);
		
		assertEquals("track has point", 1, track_1.size());

	}
	
	public void testCreatePoint()
	{
		GeometryBuilder builder = new GeometryBuilder( DefaultGeographicCRS.WGS84 );
		Point point = builder.createPoint( 48.44, -123.37 );
		Assert.assertNotNull(point);
		
		Hints hints = new Hints( Hints.CRS, DefaultGeographicCRS.WGS84 );
		PrimitiveFactory primitiveFactory = GeometryFactoryFinder.getPrimitiveFactory( hints );
		Point point2 = primitiveFactory.createPoint(  new double[]{48.44, -123.37} );
		Assert.assertNotNull(point2);
	}

	public void testRangeCalc()
	{

		GeodeticCalculator calc= GeoSupport.getCalculator();

		Point p1 = GeoSupport.getBuilder().createPoint(0, 80);
		Point p2 = GeoSupport.getBuilder().createPoint(0, 81);
		Point p3 = GeoSupport.getBuilder().createPoint(1, 80);
		
		calc.setStartingGeographicPoint(p1.getCentroid().getOrdinate(0), p1.getCentroid().getOrdinate(1));
		calc.setDestinationGeographicPoint(p2.getCentroid().getOrdinate(0), p2.getCentroid().getOrdinate(1));
		
		final double dest1 = calc.getOrthodromicDistance();
		
		calc.setDestinationGeographicPoint(p3.getCentroid().getOrdinate(0), p3.getCentroid().getOrdinate(1));
		final double dest2 = calc.getOrthodromicDistance();
		
		assertEquals("range 1 right", 111663, dest1, 10);
		assertEquals("range 2 right", 19393, dest2, 10);
		
	}
	
	public void testLocationCalc()
	{
		Temporal.Location loc1 = new Temporal.Location("loc1");
		Temporal.Location loc2 = new Temporal.Location("loc2");
		Temporal.Length_M len1 = new Temporal.Length_M("dummy2");
		
		List<IStoreItem> selection = new ArrayList<IStoreItem>();
		selection.add(loc1);
				
		IStore store = new InMemoryStore();;
		Collection<ICommand<IStoreItem>> ops = new DistanceBetweenTracksOperation().actionsFor(selection, store );
		assertEquals("empty collection", 0, ops.size());
		
		selection.add(len1);
		ops = new DistanceBetweenTracksOperation().actionsFor(selection, store );
		assertEquals("empty collection", 0, ops.size());
		
		selection.remove(len1);
		selection.add(loc2);
		ops = new DistanceBetweenTracksOperation().actionsFor(selection, store );
		assertEquals("empty collection", 1, ops.size());
		
		// ok, try adding some data
		GeometryBuilder builder = GeoSupport.getBuilder();
		loc1.add(1000, builder.createPoint(4, 3));
		loc2.add(2000, builder.createPoint(3, 4));

		ops = new DistanceBetweenTracksOperation().actionsFor(selection, store );
		assertEquals("empty collection", 1, ops.size());
		
		
	}
}
