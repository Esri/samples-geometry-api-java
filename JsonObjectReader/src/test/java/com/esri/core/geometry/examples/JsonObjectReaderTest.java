/*
    Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.esri.core.geometry.examples;

import com.esri.core.geometry.*;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JsonObjectReaderTest {

	//Demonstrate how to pass JSONObject instead of JsonReader, using the JsonObjectReader wrapper.
	@Test
	public void loadFromJsonObject() {
		String geoJson = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10,10,5],[20,10,5],[20,20,5],[10,20,5],[10,10,5]],[[12,12,3],[12,12,3],[12,12,3]],[[10,10,1],[12,12,1],[10,10,1]]],[[[90,90,88],[60,90,7],[60,60,7],[90,90,88]],[[70,70,7],[70,80,7],[80,80,7],[70,70,7]]]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:3857\"}}}";
		JSONObject obj = new JSONObject(geoJson);
		//Wrap JSONObject into a class that present it as a JSONReader:
		JsonReader reader = new JsonObjectReader(obj);
		MapGeometry geomFromJsonObject = OperatorImportFromGeoJson.local().execute(0, Geometry.Type.Unknown, reader, null);
		assertTrue(geomFromJsonObject.getGeometry().getType() == Geometry.Type.Polygon);
		MapGeometry geom2 = OperatorImportFromGeoJson.local().execute(0, Geometry.Type.Unknown, geoJson, null);
		assertTrue(geom2.equals(geomFromJsonObject));
	}

}
