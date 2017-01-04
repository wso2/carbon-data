package es.eci.carbon.datasource.reader.mongo;

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;

import com.mongodb.MongoClient;

public class MongoDataSourceReader implements DataSourceReader {

	public static final String DATASOURCE_TYPE = "MONGO";

	@Override
	public String getType() {
		return DATASOURCE_TYPE;
	}

	@Override
	public Object createDataSource(String xmlConfig, boolean isDataSourceFactoryReference) throws DataSourceException {
		return MongoDataSourceReaderUtil.loadConfiguration(xmlConfig);
	}

	@Override
	public boolean testDataSourceConnection(String xmlConfig) throws DataSourceException {
		MongoClient mongoClient = (MongoClient) this.createDataSource(xmlConfig, true);
		boolean status = false;
		if (mongoClient.getAddress() != null) {
			status = true;
		}
		mongoClient.close();
		return status;
	}

}
