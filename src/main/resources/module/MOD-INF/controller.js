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

var html = "text/html";
var encoding = "UTF-8";
var ClientSideResourceManager = Packages.com.google.refine.ClientSideResourceManager;
var logger = Packages.org.slf4j.LoggerFactory.getLogger("geojson-export");

function registerExporters() {
    logger.trace("Registering the GeoJSON exporter...");
    var ExporterRegistry = Packages.com.google.refine.exporters.ExporterRegistry;
    var GeoJSONExporter = Packages.com.google.refine.geojson.exporters.GeoJSONExporter;

    ExporterRegistry.registerExporter("geojson", new GeoJSONExporter());

    logger.trace("GeoJSON exporter registered successfully.");
}

function registerCommands() {
    logger.trace("Initializing GeoJSON commands...");
    var RefineServlet = Packages.com.google.refine.RefineServlet;
    RefineServlet.registerCommand(module, "export-to-geojson", new Packages.com.google.refine.geojson.commands.ExportGeoJSONCommand());
    logger.trace("Finished initializing GeoJSON commands.");
}

/*
 * Function invoked to initialize the extension.
 */

function init() {
    logger.trace("Initializing GeoJSON extension...");
    logger.trace(module.getMountPoint());

    registerCommands();
    registerExporters();

    ClientSideResourceManager.addPaths(
        "project/scripts",
        module,
        [
            "scripts/exporter.js",
            "scripts/dialog.js"
        ]
    );

    ClientSideResourceManager.addPaths(
        "project/styles",
        module,
        [
            "styles/theme.less",
            "styles/dialog.less"
        ]
    );
}

/*
 * Function invoked to handle each request in a custom way.
 */
function process(path, request, response) {
    // Analyze path and handle this request yourself.

    if (path == "/" || path == "") {
        const context = {};

        context.someInt = 5;
        context.someString = "foofoo";
        context.someList = ["Superior", "Michigan", "Huron", "Erie", "Ontario"];

        send(request, response, "index.vt", context);
    }
}

function send(request, response, template, context) {
    butterfly.sendTextFromTemplate(request, response, context, template, encoding, html);
}
