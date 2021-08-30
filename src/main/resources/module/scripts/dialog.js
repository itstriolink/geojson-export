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

function GeoJSONExporterDialog() {
    this._createDialog();
}

GeoJSONExporterDialog.prototype._createDialog = function () {
    var self = this;

    this._dialog = $(DOM.loadHTML("geojson-export", "scripts/dialog.html"));
    this._elmts = DOM.bind(this._dialog);
    this._level = DialogSystem.showDialog(this._dialog);

    this._elmts.dialogHeader.html($.i18n('geojson-export/geojson-exporter'));
    this._elmts.enterFileName.html($.i18n('geojson-export/enter-file-name'));
    this._elmts.selectProps.html($.i18n('geojson-export/select-columns-for-properties'));

    this._elmts.selectGeometryCols.html($.i18n('geojson-export/select-geometry-columns'));

    this._elmts.selectLat.html($.i18n('geojson-export/select-latitude-column'));
    this._elmts.selectLon.html($.i18n('geojson-export/select-longitude-column'));
    this._elmts.selectWKT.html($.i18n('geojson-export/select-wkt-column'));

    this._elmts.selectAllButton.html($.i18n('core-buttons/select-all'));
    this._elmts.deselectAllButton.html($.i18n('core-buttons/deselect-all'));

    this._elmts.geometryNumericScaleLabel.html($.i18n("geojson-export/geometry-numeric-scale"));
    this._elmts.exportButton.html($.i18n('geojson-export/export-button'));
    this._elmts.cancelButton.html($.i18n('core-buttons/cancel'));

    this._elmts.fileNameInput.val(theProject.metadata.name.replace(/\W/g, ' ').replace(/\s+/g, '_'));
    self._generateSelectElements();


    for (var i = 0; i < theProject.columnModel.columns.length; i++) {
        var column = theProject.columnModel.columns[i];
        var name = column.name;
        var selectValues = [];
        $("select").each(function (i, v) {
            var value = $(v);
            selectValues.push(value.val());
        });

        if (!selectValues.includes(name)) {
            var div = $('<div>')
                .addClass("geojson-exporter-dialog-row")
                .attr("column", name)
                .appendTo(this._elmts.columnList);

            $('<input>')
                .attr('type', 'checkbox')
                .prop('checked', false)
                .appendTo(div);

            $('<span>')
                .text(name)
                .appendTo(div);
        }

    }

    this._elmts.selectAllButton.click(function () {
        self._elmts.columnList.find('input[type="checkbox"]').prop('checked', true);
    });
    this._elmts.deselectAllButton.click(function () {
        self._elmts.columnList.find('input[type="checkbox"]').prop('checked', false);
    });


    this._elmts.cancelButton.click(function () {
        self._dismiss();
    });
    this._elmts.exportButton.click(function () {
        self._exportData();
    });
};


GeoJSONExporterDialog.prototype._dismiss = function () {
    DialogSystem.dismissUntil(this._level - 1);
};

GeoJSONExporterDialog.prototype._exportData = function () {
    const options = this._getOptionCode();
    const fileName = this._elmts.fileNameInput.val();

    const form = this._prepareExportRows(options.format, options.extension, fileName || theProject.metadata.name, options);

    document.body.appendChild(form);

    window.open(" ", "refine-export");
    form.submit();

    document.body.removeChild(form);
    this._dismiss();
};

GeoJSONExporterDialog.prototype._prepareExportRows = function (format, ext, name, options) {
    var name = encodeURI(ExporterManager.stripNonFileChars(name));
    var form = document.createElement("form");
    $(form)
        .css("display", "none")
        .attr("method", "post")
        .attr("action", "command/core/export-rows/" + name + ((ext) ? ("." + ext) : ""))
        .attr("target", "refine-export");

    $('<input />')
        .attr("name", "project")
        .val(theProject.id)
        .appendTo(form);
    $('<input />')
        .attr("name", "format")
        .val(format)
        .appendTo(form);
    $('<input />')
        .attr("name", "quoteAll")
        .appendTo(form);

    $('<input />')
        .attr("name", "engine")
        .val(JSON.stringify(ui.browsingEngine.getJSON()))
        .appendTo(form);

    $('<input />')
        .attr("name", "options")
        .val(JSON.stringify(options))
        .appendTo(form);


    return form;
}

GeoJSONExporterDialog.prototype._getOptionCode = function () {
    const options = {
        format: 'geojson',
        extension: 'geojson',
        encoding: 'UTF-8',
        outputColumnHeaders: false,
        outputBlankRows: false,
        geometryNumericScale: this._elmts.geometryNumericScaleInput.val(),
        latitudeColumn: $("select#selectLatitude").val(),
        longitudeColumn: $("select#selectLongitude").val(),
        wktColumn: $("select#selectWKT").val(),
        propertyColumns: []
    };

    this._elmts.columnList.find('.geojson-exporter-dialog-row').each(function () {
        if ($(this).find('input[type="checkbox"]')[0].checked) {
            options.propertyColumns.push({
                name: this.getAttribute('column')
            });
        }
    });

    return options;
};

GeoJSONExporterDialog.prototype._generateSelectElements = function () {
    var self = this;
    var latitudeSel = $('<select>').appendTo('body');
    var longitudeSel = $('<select>').appendTo('body');
    var wktSel = $("<select>").appendTo("body");
    latitudeSel.attr("id", "selectLatitude");
    latitudeSel.css("width", "50%");

    longitudeSel.attr("id", "selectLongitude");
    longitudeSel.css("width", "50%");

    wktSel.attr("id", "selectWKT");
    wktSel.css("width", "50%");


    var previous;
    $(latitudeSel).add(longitudeSel).add(wktSel).on("focus click", function () {
        previous = this.value;
    }).change(function () {
        var value = this.value;
        $('.geojson-exporter-dialog-row[column="' + value + '"]').remove();
        if (previous && latitudeSel.val() != previous && longitudeSel.val() != previous && wktSel.val() != previous) {
            var div = $('<div>')
                .addClass("geojson-exporter-dialog-row")
                .attr("column", previous)
                .appendTo(self._elmts.columnList);

            $('<input>')
                .attr('type', 'checkbox')
                .prop('checked', false)
                .appendTo(div);

            $('<span>')
                .text(previous)
                .appendTo(div);
        }
    });

    latitudeSel.append($("<option>").val("").text(""));
    longitudeSel.append($("<option>").val("").text(""));
    wktSel.append($("<option>").val("").text(""));

    for (var i = 0; i < theProject.columnModel.columns.length; i++) {
        var column = theProject.columnModel.columns[i];

        latitudeSel.append($("<option>").val(column.name).text(column.name));
        longitudeSel.append($("<option>").val(column.name).text(column.name));
        wktSel.append($("<option>").val(column.name).text(column.name));

        if (column.name.match(new RegExp(/^lat.*$/gi))) {
            latitudeSel.val(column.name);
        }
        if (column.name.match(new RegExp(/^lon.*$/gi))) {
            longitudeSel.val(column.name);
        }

        if (column.name.match(new RegExp(/^wkt.*$/gi))) {
            wktSel.val(column.name);
        }
    }

    latitudeSel.appendTo($("#select-latitude-column")[0]);
    longitudeSel.appendTo($("#select-longitude-column")[0]);
    wktSel.appendTo($("#select-wkt-column")[0])
}

