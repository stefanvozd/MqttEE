package rs.mqttserver.client;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class LoggingService {

	final static Logger log = Logger.getLogger(LoggingService.class);
	private static String logging_topic_prefix = "";
	private static int logging_topic_qos = 0;

	private static MqttAsyncClient cli;
	
	static {
		 try {
			cli =MqttClientFactory.getConnectedMqttAsyncClient();
			logging_topic_prefix = Prop.getInstance().getValue("logging_topic_prefix");
			 logging_topic_qos = Integer.parseInt(Prop.getInstance().getValue("logging_topic_qos"));
		} catch (MqttException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public enum LogLevel {
	    INFO, WARNING, ERROR, CRITICAL
	}

	
	//topic={logging_topic_prefix}/{deviceid}/logging/alert/{level}   qos=1
	public static void alert(LogLevel lvl,String message) {
		 logCommon(false,"alert",lvl,message);
	}

	//topic={logging_topic_prefix}/{deviceid}/logging/activity/{level}   qos=1
	public static void activity(LogLevel lvl,String message) {
		 logCommon(false,"activity",lvl,message);
	}
	
	//Non blocking if connection is broken > FireAndForget
	public static void alert_FireAndForget(LogLevel lvl,String message) {
		 logCommon(true,"alert",lvl,message);
	}

	//Non blocking if connection is broken > FireAndForget
	public static void activity_FireAndForget(LogLevel lvl,String message) {
		 logCommon(true,"activity",lvl,message);
	}

	
	private static void logCommon(boolean fireandforget,String type,LogLevel l,String message){
		try {
			String deviceid = Prop.getInstance().getValue("mqtt_clientid");
			String loglvl = getLogLevelString(l);
			String topic = logging_topic_prefix+"/"+deviceid+"/"+type+"/"+loglvl;
			if(fireandforget) cli.publish(topic,message.getBytes(),logging_topic_qos,true);
			else MqttClientFactory.getConnectedMqttAsyncClient().publish(topic,message.getBytes(),logging_topic_qos,false);
		} catch (MqttException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private static String getLogLevelString(LogLevel lvl){
		switch (lvl) {
		case INFO: return "info";
		case WARNING: return "warning";
		case ERROR: return "error";
		case CRITICAL: return "critical";
		default: return "error";
		}
	}
}

