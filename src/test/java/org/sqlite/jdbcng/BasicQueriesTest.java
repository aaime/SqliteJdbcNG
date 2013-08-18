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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BasicQueriesTest {
    private final SqliteDriver driver = new SqliteDriver();
    private Connection conn;

    @Before
    public void getConnection() throws SQLException {
        this.conn = this.driver.connect("jdbc:sqlite:", null);
    }

    @After
    public void closeConnection() throws SQLException {
        this.conn.close();
        this.conn = null;
    }

    @Test
    public void testSimpleQueries() throws Exception {
        ResultSet rs;
        try (Statement stmt = conn.createStatement()) {
            assertEquals(conn, stmt.getConnection());

            assertEquals("jdbc:sqlite:", conn.getMetaData().getURL());

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS test_table " +
                    "(id INTEGER PRIMARY KEY, name TEXT, start DATETIME)");

            rs = stmt.executeQuery("SELECT * FROM test_table");

            assertEquals(false, rs.next());

            try {
                stmt.executeQuery("THIS IS BAD SQL");
                fail("SQL Syntax error was not thrown");
            } catch (SQLSyntaxErrorException e) {

            }

            assertEquals(true, conn.getAutoCommit());

            int rc = stmt.executeUpdate("INSERT INTO test_table VALUES (1, 'Kino', '2010-05-25T10:00:00')");

            assertEquals(1, rc);

            assertEquals(true, conn.getAutoCommit());
            assertEquals(false, conn.isReadOnly());

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM test_table WHERE id=?");

            ps.setInt(1, 2);
            rs = ps.executeQuery();

            assertEquals(false, rs.next());

            rs.close();

            ps.setInt(1, 1);
            rs = ps.executeQuery();

            assertEquals(true, rs.next());

            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt("id"));
            assertEquals("Kino", rs.getString(2));
            assertEquals("Kino", rs.getString("name"));

            assertEquals(false, rs.next());

            ps.close();

            ps = conn.prepareStatement("INSERT INTO test_table VALUES (?, ?, ?)");
            ps.setInt(1, 2);
            ps.setString(2, "Eve");
            Timestamp ts = new Timestamp(1376222713L * 1000L);

            ps.setTimestamp(3, ts);

            rc = ps.executeUpdate();
            assertEquals(1, rc);
            assertEquals(1, ps.getUpdateCount());

            rs = stmt.executeQuery("SELECT * FROM test_table ORDER BY id DESC");

            assertEquals(true, rs.next());
            // XXX assertEquals("2013-08-11 05:05:13.000", rs.getString(3));

            ps.close();
        }
    }
}
