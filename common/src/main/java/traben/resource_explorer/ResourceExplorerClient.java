package traben.resource_explorer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceExplorerClient
{
	public static final String MOD_ID = "resource_explorer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {

	}

	public static void log(Object message){
		LOGGER.info("[resource_explorer]: " + message.toString());
	}
	public static void logWarn(Object message){
		LOGGER.warn("[resource_explorer]: " + message.toString());
	}
	public static void logError(Object message){
		LOGGER.error("[resource_explorer]: " + message.toString());
	}


}
