package org.xsnake.logs;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.xsnake.common.BaseDaoUtil;

public abstract class DatabaseLogs {

	private ExecutorService service =  Executors.newFixedThreadPool(2);
	
	abstract String getCreateTableSQL();
	
	abstract String getInsertSQL();
	
	abstract Object[] getArgs();
	
	abstract String getCurrentTableName();
	
	abstract String getTemplateTableName();
	
	public void log(){
		Thread t = new Thread(){
			public void run() {
				try {
					BaseDaoUtil.executeUpdate(getInsertSQL(), getArgs());
				} catch (SQLException e) {
					if(e.getErrorCode() == 1146){
						try {
							BaseDaoUtil.executeUpdate("CREATE TABLE "+getCurrentTableName()+" SELECT * FROM "+getTemplateTableName()+" WHERE 1=0 ",null);
							BaseDaoUtil.executeUpdate(getInsertSQL(), getArgs());
						} catch (SQLException e1) {
							if(e1.getErrorCode() == 1146){
								try {
									BaseDaoUtil.executeUpdate(getCreateTableSQL(),null);
									BaseDaoUtil.executeUpdate(getInsertSQL(), getArgs());
								} catch (SQLException e2) {
								}
							}
						}
					}
				}
			};
		};
		service.execute(t);
	}
	
}
