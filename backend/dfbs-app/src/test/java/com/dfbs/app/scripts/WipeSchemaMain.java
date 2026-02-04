package com.dfbs.app.scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * One-off main to wipe public schema so Flyway can rebuild from scratch.
 * Run: mvn -q exec:java -Dexec.mainClass="com.dfbs.app.scripts.WipeSchemaMain" -Dexec.classpathScope=test
 * Or set env: WIPE_DB_URL, WIPE_DB_USER, WIPE_DB_PASSWORD (default: localhost:5432/dfbs, dfbs, dfbs).
 */
public class WipeSchemaMain {

	public static void main(String[] args) throws Exception {
		String url = System.getenv("WIPE_DB_URL");
		if (url == null || url.isBlank()) {
			url = "jdbc:postgresql://localhost:5432/dfbs";
		}
		String user = System.getenv("WIPE_DB_USER");
		if (user == null || user.isBlank()) user = "dfbs";
		String password = System.getenv("WIPE_DB_PASSWORD");
		if (password == null) password = "dfbs";

		try (Connection c = DriverManager.getConnection(url, user, password);
		     Statement s = c.createStatement()) {
			s.execute("DROP SCHEMA public CASCADE");
			s.execute("CREATE SCHEMA public");
			System.out.println("Schema wiped: public (DROP CASCADE + CREATE).");
		}
	}
}
