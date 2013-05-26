environments {
    // TODO: Is it needed?
    test {
    	dataSource {
        	driverClassName = "org.hsqldb.jdbcDriver"
        	username = "sa"
        	password = GString.EMPTY
    		dbCreate = "create-drop"
    		url = "jdbc:hsqldb:mem:testDb"
    	}
    }
}