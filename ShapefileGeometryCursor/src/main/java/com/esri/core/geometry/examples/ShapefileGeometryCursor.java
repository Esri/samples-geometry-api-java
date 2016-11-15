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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * Reads a shapefile
 * Created by willtemperley@gmail.com on 07-Nov-16.
 */
public class ShapefileGeometryCursor extends GeometryCursor {

    private final MixedEndianDataInputStream inputStream;
    private final Envelope2D envelope2D;

    private OperatorImportFromESRIShape importFromESRIShape = OperatorImportFromESRIShape.local();
    private final int fileLengthBytes;
    private int position = 0; //keeps track of where inputstream is
    private int recordNumber; //the record number according to shapefile

    private final Geometry.Type geomType;

    @Override
    public Geometry next() {
        if (! hasNext()) {
            return null;
        }
        try {

            recordNumber = inputStream.readInt();//1 based
            int recLength = inputStream.readInt();
            position += 8;

            int recordSizeBytes = (recLength * 2);
            byte[] bytes = new byte[recordSizeBytes];
            int read = inputStream.read(bytes);

            Geometry polyline = importFromESRIShape.execute(0, geomType, ByteBuffer.wrap(bytes));
            position += recordSizeBytes;

            return polyline;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getGeometryID() {
        return recordNumber;
    }

    public Envelope2D getEnvelope2D() {
        return envelope2D;
    }

    public ShapefileGeometryCursor(File inFile) throws IOException {
        this(new FileInputStream(inFile));
    }

    public ShapefileGeometryCursor(InputStream in) throws IOException {

        this.inputStream = new MixedEndianDataInputStream(in);

        /*
        Byte 0 File Code 9994 Integer Big
         */
        int fileCode = inputStream.readInt();
        if (fileCode != 9994) {
            throw new IOException("file code " + fileCode + " is not supported.");
        }

        /*
        Byte 4 Unused 0 Integer Big
        Byte 8 Unused 0 Integer Big
        Byte 12 Unused 0 Integer Big
        Byte 16 Unused 0 Integer Big
        Byte 20 Unused 0 Integer Big
         */
        inputStream.skipBytes(20);

        fileLengthBytes = inputStream.readInt() * 2;

        int v = this.inputStream.readLittleEndianInt();

        if (v != 1000) {
            throw new IOException("version " + v + " is not supported.");
        }

        int shpTypeId = this.inputStream.readLittleEndianInt();
        geomType = geometryTypeFromShpType(shpTypeId);

        /*
        Byte 24 File Length File Length Integer Big
        Byte 28 Version 1000 Integer Little
        Byte 32 Shape Type Shape Type Integer Little
        Byte 36 Bounding Box Xmin Double Little
        Byte 44 Bounding Box Ymin Double Little
        Byte 52 Bounding Box Xmax Double Little
        Byte 60 Bounding Box Ymax Double Little
        Byte 68* Bounding Box Zmin Double Little
        Byte 76* Bounding Box Zmax Double Little
        Byte 84* Bounding Box Mmin Double Little
        Byte 92* Bounding Box Mmax Double Little
        */
        double xmin = this.inputStream.readLittleEndianDouble();
        double ymin = this.inputStream.readLittleEndianDouble();
        double xmax = this.inputStream.readLittleEndianDouble();
        double ymax = this.inputStream.readLittleEndianDouble();
        double zmin = this.inputStream.readLittleEndianDouble();
        double zmax = this.inputStream.readLittleEndianDouble();
        double mmin = this.inputStream.readLittleEndianDouble();
        double mmax = this.inputStream.readLittleEndianDouble();

        envelope2D = new Envelope2D(xmin, ymin, xmax, ymax);
//        envelope3D = new Envelope3D(xmin, ymin, zmin, xmax, ymax, zmax);

        position = 2 * 50; //header is always 50 words long

    }

    /**
     from esri spec:
     0 Null Shape
     1 Point
     3 PolyLine
     5 Polygon
     8 MultiPoint
     11 PointZ
     13 PolyLineZ
     15 PolygonZ
     18 MultiPointZ
     21 PointM
     23 PolyLineM
     25 PolygonM
     28 MultiPointM
     31 MultiPatch
     therefore final digit suffices to determine type (PolyLine, PolyLineM, PolylineZ are 1, 13 and 23 respectively).
     *
     * @param shpTypeId shape type id from shapfile
     * @return the geom type
     */
    private Geometry.Type geometryTypeFromShpType(int shpTypeId) {
        int shpType = shpTypeId % 10;

        switch (shpType) {
            case 1: //Point
                return Geometry.Type.Point;
            case 3: //Polyline
                return Geometry.Type.Polyline;
            case 5: //Polygon
                return Geometry.Type.Polygon;
            case 8: //Multipoint
                return Geometry.Type.MultiPoint;
            default:
                return Geometry.Type.Unknown;
        }
    }


    public boolean hasNext() {
        return position < fileLengthBytes;
    }

    public Geometry.Type getGeometryType() {
        return geomType;
    }
}
