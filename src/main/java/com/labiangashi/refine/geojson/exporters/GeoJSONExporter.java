/*
 * MIT License
 *
 * Copyright (c) 2021 Labian Gashi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.labiangashi.refine.geojson.exporters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.refine.browsing.Engine;
import com.google.refine.exporters.CustomizableTabularExporterUtilities;
import com.google.refine.exporters.TabularSerializer;
import com.google.refine.exporters.WriterExporter;
import com.google.refine.model.Project;
import com.google.refine.util.JSONUtilities;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Point;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.*;


public class GeoJSONExporter implements WriterExporter {
    final static Logger logger = LoggerFactory.getLogger("GeoJSONExporter");
    private final WKTReader wktReader;
    private final double[] emptyLatLon = new double[]{0.0, 0.0};
    GeoJSONWriter geoJSONWriter;
    ArrayList<Feature> features;
    FeatureCollection featureCollection;
    List<String> propertyColumns;
    String latitudeColumn = "";
    String longitudeColumn = "";
    String wktColumn = "";
    int geometryNumericScale;
    double scaleFactor;


    public GeoJSONExporter() {
        wktReader = new WKTReader();
        geoJSONWriter = new GeoJSONWriter();
    }

    @Override
    public String getContentType() {
        return "application/geo+json";
    }

    @Override
    public void export(final Project project, Properties params, Engine engine, Writer writer) {
        propertyColumns = new ArrayList<>();
        features = new ArrayList<>();

        TabularSerializer serializer = new TabularSerializer() {
            @Override
            public void startFile(JsonNode options) {
                latitudeColumn = JSONUtilities.getString(options, "latitudeColumn", null);
                longitudeColumn = JSONUtilities.getString(options, "longitudeColumn", null);
                wktColumn = JSONUtilities.getString(options, "wktColumn", null);
                geometryNumericScale = JSONUtilities.getInt(options, "geometryNumericScale", 7);

                if (geometryNumericScale < 0 || geometryNumericScale > 10) {
                    geometryNumericScale = 7;
                }

                scaleFactor = 1.0D;
                for (int i = 0; i < geometryNumericScale; i++) {
                    scaleFactor *= 10.0D;
                }

                List<JsonNode> array = JSONUtilities.getArray(options, "propertyColumns");
                for (JsonNode columnOptions : array) {
                    if (columnOptions != null) {
                        String name = JSONUtilities.getString(columnOptions, "name", null);
                        if (name != null) {
                            propertyColumns.add(name);
                        }
                    }
                }
            }

            @Override
            public void endFile() {
                FeatureCollection featureCollection = geoJSONWriter.write(features);
                try {
                    writer.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(featureCollection));
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("GeoJSONExporter::Export::EndFile::Exception::{}", e);
                }
            }

            @Override
            public void addRow(List<CellData> cells, boolean isHeader) {
                double latitude = 0.0d;
                double longitude = 0.0d;
                Geometry jtsGeometry = null;
                Map<String, Object> properties = new HashMap<>();

                for (CellData cellData : cells) {
                    if (cellData != null && cellData.text != null && cellData.columnName != null) {
                        if (cellData.columnName.equals(latitudeColumn)) {
                            try {
                                latitude = Double.parseDouble(cellData.text);
                                latitude = Math.round(latitude * scaleFactor) / scaleFactor;
                            } catch (NumberFormatException nfe) {
                                logger.error("The '" + cellData.text + "' value on the '" + cellData.columnName + "' column could not be parsed to a latitude coordinate.");
                            }
                        } else if (cellData.columnName.equals(longitudeColumn)) {
                            try {
                                longitude = Double.parseDouble(cellData.text);
                                longitude = Math.round(longitude * scaleFactor) / scaleFactor;
                            } catch (NumberFormatException nfe) {
                                logger.error("The '" + cellData.text + "' value on the '" + cellData.columnName + "' column could not be parsed to a longitude coordinate.");
                            }
                        } else if (cellData.columnName.equals(wktColumn)) {
                            try {
                                jtsGeometry = wktReader.read(cellData.text);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                logger.error("GeoJSONExporter::Export::AddRow::Exception::{}", e);
                            }
                        } else if (propertyColumns.contains(cellData.columnName)) {
                            properties.put(cellData.columnName, cellData.text);
                        } else {
                        }
                    }
                }

                if (longitude > 0.0d && latitude > 0.0d) {
                    Feature feature = new Feature(new Point(new double[]{longitude, latitude}), properties);
                    features.add(feature);
                } else if (jtsGeometry != null) {
                    org.wololo.geojson.Geometry geometry = geoJSONWriter.write(jtsGeometry);

                    Class<? extends org.wololo.geojson.Geometry> c = geometry.getClass();
                    if (c.equals(org.wololo.geojson.Point.class)) {
                        org.wololo.geojson.Point point = (org.wololo.geojson.Point) geometry;
                        double[] coordinates = point.getCoordinates();
                        double[] _coordinates = new double[]{
                                Math.round(coordinates[0] * scaleFactor) / scaleFactor,
                                Math.round(coordinates[1] * scaleFactor) / scaleFactor
                        };

                        org.wololo.geojson.Point _point = new org.wololo.geojson.Point(_coordinates);

                        features.add(new Feature(_point, properties));
                    } else if (c.equals(org.wololo.geojson.LineString.class)) {
                        org.wololo.geojson.LineString lineString = (org.wololo.geojson.LineString) geometry;
                        double[][] coordinates = lineString.getCoordinates();

                        for (int i = 0; i < coordinates.length; i++) {
                            for (int j = 0; j < coordinates[i].length; j++) {
                                coordinates[i][j] = Math.round(coordinates[i][j] * scaleFactor) / scaleFactor;
                            }
                        }

                        features.add(new Feature(new org.wololo.geojson.LineString(coordinates), properties));
                    } else if (c.equals(org.wololo.geojson.Polygon.class)) {
                        org.wololo.geojson.Polygon polygon = (org.wololo.geojson.Polygon) geometry;
                        double[][][] coordinates = polygon.getCoordinates();

                        for (int i = 0; i < coordinates.length; i++) {
                            for (int j = 0; j < coordinates[i].length; j++) {
                                for (int k = 0; k < coordinates[i][j].length; k++) {
                                    coordinates[i][j][k] = Math.round(coordinates[i][j][k] * scaleFactor) / scaleFactor;
                                }
                            }
                        }

                        features.add(new Feature(new org.wololo.geojson.Polygon(coordinates), properties));
                    } else if (c.equals(org.wololo.geojson.MultiPoint.class)) {
                        org.wololo.geojson.MultiPoint multiPoint = (org.wololo.geojson.MultiPoint) geometry;
                        double[][] coordinates = multiPoint.getCoordinates();

                        for (int i = 0; i < coordinates.length; i++) {
                            for (int j = 0; j < coordinates[i].length; j++) {
                                coordinates[i][j] = Math.round(coordinates[i][j] * scaleFactor) / scaleFactor;
                            }
                        }

                        features.add(new Feature(new org.wololo.geojson.MultiPoint(coordinates), properties));
                    } else if (c.equals(org.wololo.geojson.MultiLineString.class)) {
                        org.wololo.geojson.MultiLineString multiLineString = (org.wololo.geojson.MultiLineString) geometry;
                        double[][][] coordinates = multiLineString.getCoordinates();

                        for (int i = 0; i < coordinates.length; i++) {
                            for (int j = 0; j < coordinates[i].length; j++) {
                                for (int k = 0; k < coordinates[i][j].length; k++) {
                                    coordinates[i][j][k] = Math.round(coordinates[i][j][k] * scaleFactor) / scaleFactor;
                                }
                            }
                        }

                        features.add(new Feature(new org.wololo.geojson.MultiLineString(coordinates), properties));
                    } else if (c.equals(org.wololo.geojson.MultiPolygon.class)) {
                        org.wololo.geojson.MultiPolygon multiPolygon = (org.wololo.geojson.MultiPolygon) geometry;
                        double[][][][] coordinates = multiPolygon.getCoordinates();

                        for (int i = 0; i < coordinates.length; i++) {
                            for (int j = 0; j < coordinates[i].length; j++) {
                                for (int k = 0; k < coordinates[i][j].length; k++) {
                                    for (int l = 0; l < coordinates[i][j][k].length; l++) {
                                        coordinates[i][j][k][l] = Math.round(coordinates[i][j][k][l] * scaleFactor) / scaleFactor;
                                    }
                                }
                            }
                        }

                        features.add(new Feature(new org.wololo.geojson.MultiPolygon(coordinates), properties));
                    } else {

                    }
                } else {

                }
            }
        };

        CustomizableTabularExporterUtilities.exportRows(project, engine, params, serializer);
    }
}
