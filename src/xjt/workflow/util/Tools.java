package xjt.workflow.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Tools {
	public static java.util.Date getDate(SessionFactory sf) {
		Connection conn = null;
		Session session = null;
		try {
			session = sf.openSession();
			conn = session.connection();
			return getDate(conn);
		} finally {
			//close(conn);
			//关了session就不需要关conn了
			close(session);
		}
	}
	public static java.util.Date getDate(Connection conn) {

		String sql = null;
		String sProductName = null;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			sProductName = conn.getMetaData().getDatabaseProductName()
					.toLowerCase();

			if (sProductName.indexOf("oracle") != -1) {
				//Oracle (Oracle)
				sql = "select systimestamp from dual";
			} else if (sProductName.indexOf("sql server") != -1) {
				//SQL Server (Microsoft SQL Server)
				sql = "select getdate()";
			} else if (sProductName.indexOf("adaptive server") != -1) {
				//Sybase (Adaptive Server Enterprise)
				sql = "select getdate()";

				//下面的数据库没经过测试。
			} else if (sProductName.indexOf("db2") != -1) {
				//DB2
				sql = "SELECT CURRENT TIMESTAMP FROM SYSIBM.SYSDUMMY1";
			} else if (sProductName.indexOf("informix") != -1) {
				//INFORMIX
				sql = "select today";
			} else if (sProductName.indexOf("mysql") != -1) {
				//MYSQL
				sql = "select now()";
			} else {
				//其他
				sql = "select getdate()";
			}

			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getTimestamp(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(stmt);
		}
		return null;
	}
	public static void close(Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void close(Session session) {
		try {
			if (session != null) {
				session.close();
			}
		}
		catch (Exception e) {
			//什么也不做
		}
	}
}
