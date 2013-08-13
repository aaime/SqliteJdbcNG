/*
 * Copyright (c) 2013, Timothy Stack
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  Neither the name of Timothy Stack nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sqlite.jdbcng;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SqliteDriverTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Driver driver;

    @Before
    public void newDriver() {
        this.driver = new SqliteDriver();
    }

    @Test
    public void testAcceptsUrl() throws Exception {
        assertEquals(false, driver.acceptsURL(""));
        assertEquals(false, driver.acceptsURL("jdbc:sqlite"));
        assertEquals(true, driver.acceptsURL("jdbc:sqlite:"));
        assertEquals(true, driver.acceptsURL("jdbc:sqlite::memory:"));
        assertEquals(true, driver.acceptsURL("jdbc:sqlite:/tmp/test.db"));
    }

    @Test
    public void testVersion() {
        assertEquals(SqliteDriver.VERSION[0], this.driver.getMajorVersion());
        assertEquals(SqliteDriver.VERSION[1], this.driver.getMinorVersion());
    }

    @Test(expected = SQLTransientConnectionException.class)
    public void testUrlDoesNotExist() throws Exception {
        driver.connect("jdbc:sqlite:/non-existent/path/to/db", null);
    }

    @Test(expected = SQLNonTransientConnectionException.class)
    public void testNotADB() throws Exception {
        File tempFile = testFolder.newFile("file.txt");

        try (PrintWriter pw = new PrintWriter(tempFile)) {
            pw.println("Hello, World!");
        }
        driver.connect("jdbc:sqlite:" + tempFile.getAbsolutePath(), null);
    }

    @Test(expected = SQLTransientConnectionException.class)
    public void testDirectory() throws Exception {
        driver.connect("jdbc:sqlite:" + testFolder.getRoot().getAbsolutePath(), null);
    }

    @Test
    public void testEmptyUrl() throws Exception {
        assertEquals(null, driver.connect("", null));
    }

    @Test(expected = SQLTransientConnectionException.class)
    public void testNoPermissions() throws Exception {
        File tempFile = testFolder.newFile("test.db");

        tempFile.setReadable(false);
        driver.connect("jdbc:sqlite:" + tempFile.getAbsolutePath(), null);
    }

    @Test
    public void testWorking() throws Exception {
        try (Connection conn = driver.connect("jdbc:sqlite:", null)) {
            assertEquals(null, conn.getWarnings());
        }
    }

    @Test
    public void testCompliance() throws Exception {
        assertEquals(false, this.driver.jdbcCompliant());
    }

    @Test
    public void testLogger() throws Exception {
        assertNotNull(this.driver.getParentLogger());
    }
}
