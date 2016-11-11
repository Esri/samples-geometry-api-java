package com.esri.core.geometry.examples;

import com.esri.core.geometry.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ShapefileGeometryCursorTest {

    /**
     * The shapefile reader implements {@link GeometryCursor} so can be used directly as input to spatial operations
     */
    @Test
    public void spatialOpsWithShapefileAsGeometryCursor() throws IOException {

        {
            File file = getTestShapefile("polygons");

            ShapefileGeometryCursor polygonShapefileCursor = new ShapefileGeometryCursor(file);
            GeometryCursor unionCursor = OperatorUnion.local().execute(polygonShapefileCursor, null, null);

            Geometry dissolvedGeometry = unionCursor.next();
            Polygon multiPoly = (Polygon) dissolvedGeometry;
            assertTrue(multiPoly.getExteriorRingCount() == 4); //the test file has three polys, one with two outer rings
            assertTrue(unionCursor.next() == null);  //should only be one polygon
        }

        {
            File file = getTestShapefile("polylines");

            ShapefileGeometryCursor polylineShapefileCursor = new ShapefileGeometryCursor(file);
            GeometryCursor bufferCursor = OperatorBuffer.local().execute(polylineShapefileCursor, null, new double[]{0.5}, true, null);

            Polygon polygon = (Polygon) bufferCursor.next();
            assertTrue(bufferCursor.next() == null);//should only be one polygon as union was specified on buffer
            assertTrue(polygon.getExteriorRingCount() == 3); //test file has three lines
        }
    }

    /**
     * Read the four main types of shapefile
     */
    @Test
    public void shapefileReaderTest() throws IOException {

        {
            File file = getTestShapefile("points");
            ShapefileGeometryCursor geometryCursor = new ShapefileGeometryCursor(file);

            Geometry geom;
            List<Point> points = new ArrayList<Point>();
            while ((geom = geometryCursor.next()) != null) {
                Point point = (Point) geom;
                points.add(point);
            }
            assertTrue(points.size() == 7);

            Geometry.Type geometryType = geometryCursor.getGeometryType();
            assertTrue(geometryType.equals(Geometry.Type.Point));
        }

        {
            File file = getTestShapefile("multipoints");
            ShapefileGeometryCursor geometryCursor = new ShapefileGeometryCursor(file);

            Geometry geom;
            List<MultiPoint> multiPoints = new ArrayList<MultiPoint>();
            while ((geom = geometryCursor.next()) != null) {
                MultiPoint multiPoint = (MultiPoint) geom;
                multiPoints.add(multiPoint);
            }
            assertTrue(multiPoints.size() == 2);

            Geometry.Type geometryType = geometryCursor.getGeometryType();
            assertTrue(geometryType.equals(Geometry.Type.MultiPoint));
        }

        {
            File file = getTestShapefile("polygons");
            ShapefileGeometryCursor geometryCursor = new ShapefileGeometryCursor(file);

            Geometry geom;
            List<Polygon> polygons = new ArrayList<Polygon>();
            while ((geom = geometryCursor.next()) != null) {
                Polygon polygon = (Polygon) geom;
                polygons.add(polygon);
            }
            assertTrue(polygons.size() == 3);

            Geometry.Type geometryType = geometryCursor.getGeometryType();
            assertTrue(geometryType.equals(Geometry.Type.Polygon));
        }

        {
            File file = getTestShapefile("polylines");
            ShapefileGeometryCursor geometryCursor = new ShapefileGeometryCursor(file);

            Geometry geom;
            List<Polyline> polylines = new ArrayList<Polyline>();
            while ((geom = geometryCursor.next()) != null) {
                Polyline polyline = (Polyline) geom;
                polylines.add(polyline);
            }
            assertTrue(polylines.size() == 2);

            Geometry.Type geometryType = geometryCursor.getGeometryType();
            assertTrue(geometryType.equals(Geometry.Type.Polyline));
        }

    }

    private File getTestShapefile(String name) {
        return new File("src/test/resources/" + name + ".shp");
    }
}
