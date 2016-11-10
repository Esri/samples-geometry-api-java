package com.esri.core.geometry.examples;

import com.esri.core.geometry.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ShapefileGeometryCursorTest  {

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
