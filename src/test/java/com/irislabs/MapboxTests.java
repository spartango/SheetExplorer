package com.irislabs;

import com.spartango.loc.geo.MapboxGeocoder;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Author: spartango
 * Date: 12/3/14
 * Time: 21:48.
 */
public class MapboxTests {

    @Test
    public void testGeocodePoint() throws Exception {
        MapboxGeocoder geocoder = new MapboxGeocoder(
                "pk.eyJ1Ijoic3BhcnRhbmdvIiwiYSI6IkFvOEpBcWcifQ.YJf-kBxkS9GYW2SFQ3Bpcg");

        final Optional<Coordinate> result = geocoder.geocodePoint("anchorage");
        Assert.assertTrue(result.isPresent());
        result.map(point -> {
            Assert.assertEquals(-147.89327, point.getLongitude(), 0.01);
            Assert.assertEquals(69.219934, point.getLatitude(), 0.01);

            return point;
        });
    }

}
