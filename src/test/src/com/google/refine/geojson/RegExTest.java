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

package com.google.refine.geojson;

import com.google.refine.geojson.util.Constants;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RegExTest {
    @Test
    public void testLatitudeValueTrue() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue("47.3862091".matches(Constants.RegEx.latitudeValue));
        softAssert.assertTrue("50".matches(Constants.RegEx.latitudeValue));
        softAssert.assertTrue("50.927".matches(Constants.RegEx.latitudeValue));
        softAssert.assertAll();
    }

    @Test
    public void testLongitudeValueTrue() {
        assertTrue("8.2403654".matches(Constants.RegEx.longitudeValue));
    }

    @Test
    public void testLatitudeValueFalse() {
        assertFalse("foo".matches(Constants.RegEx.latitudeValue));
    }

    @Test
    public void testLongitudeValueFalse() {
        assertFalse("bar".matches(Constants.RegEx.longitudeValue));
    }
}
