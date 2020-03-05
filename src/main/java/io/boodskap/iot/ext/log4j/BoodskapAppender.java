package io.boodskap.iot.ext.log4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;

public class BoodskapAppender extends AppenderSkeleton implements Runnable{

    /**
     * Boodskap Base API Path
     */
    protected String apiBasePath;

    /**
     * Boodskap Domain Key
     */
    protected String domainKey;

    /**
     * Boodskap API Key
     */
    protected String apiKey;

    /**
     * Application unique ID
     */
    protected String appId;
    
    /**
     * Internal event queue size
     */
    protected int queueSize = 10000;
    
    protected String binRule = "log4j";
    protected String binPatternRule = "log4j-pattern";
    
    private LinkedBlockingQueue<String> outQ;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    public BoodskapAppender() {
    }

    
	@Override
	public void activateOptions() {
		
		if(StringUtils.isBlank(apiBasePath) || StringUtils.isBlank(domainKey) || StringUtils.isBlank(apiKey) || StringUtils.isBlank(appId)) {
			throw new RuntimeException("Expected apiBasePath, domainKey, apiKey and appId");
		}
		
		outQ = new LinkedBlockingQueue<String>(queueSize);
		exec.submit(this);
		super.activateOptions();
	}


	@Override
	public void close() {
		exec.shutdownNow();
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		try {
			
			JSONObject json = new JSONObject();
			JSONObject log = new JSONObject();
			json.put("appid", appId);
			
			log.put("created_time", event.getTimeStamp());
			//log.put("log_data", getLayout().format(event));
			log.put("log_data", String.valueOf(event.getMessage()));
			log.put("event", event.getLevel().toString());
			log.put("thread", event.getThreadName());
			
			event.getLocationInformation();
			
			if(event.locationInformationExists()) {
				log.put("clazz", event.getLocationInformation().getClassName());
				log.put("file", event.getLocationInformation().getFileName());
				log.put("line", event.getLocationInformation().getLineNumber());
				log.put("method", event.getLocationInformation().getMethodName());
			}
			
			if(null != event.getThrowableInformation()) {
				Throwable e = event.getThrowableInformation().getThrowable();
				if(null != e) {
					log.put("exception", ExceptionUtils.getStackTrace(e));
				}
			}
			
			json.put("log", log);
			
			outQ.offer(json.toString(), 10, TimeUnit.SECONDS);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			
			JSONObject json = new JSONObject();
			json.put("appid", appId);
			json.put("pattern", ((PatternLayout)getLayout()).getConversionPattern());
			
			send(json.toString(), binPatternRule);
			
			while(!Thread.currentThread().isInterrupted()) {
				String out = outQ.take();
				send(out, binRule);
			}
			
		}catch(InterruptedException iex) {
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	public String getApiBasePath() {
		return apiBasePath;
	}


	public void setApiBasePath(String apiBasePath) {
		this.apiBasePath = apiBasePath;
	}


	public String getDomainKey() {
		return domainKey;
	}


	public void setDomainKey(String domainKey) {
		this.domainKey = domainKey;
	}


	public String getApiKey() {
		return apiKey;
	}


	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	public String getAppId() {
		return appId;
	}


	public void setAppId(String appId) {
		this.appId = appId;
	}


	public int getQueueSize() {
		return queueSize;
	}


	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}


	public String getBinRule() {
		return binRule;
	}


	public void setBinRule(String binRule) {
		this.binRule = binRule;
	}


	public String getBinPatternRule() {
		return binPatternRule;
	}


	public void setBinPatternRule(String binPatternRule) {
		this.binPatternRule = binPatternRule;
	}


	private void send(String out, String rule) {
		try {
			@SuppressWarnings("unused")
			String res = Unirest.post(String.format("%s/mservice/push/bin/data/{dkey}/{akey}/-/-/-/{type}", apiBasePath))
			.routeParam("dkey", domainKey)
			.routeParam("akey", apiKey)
			.routeParam("type", rule)
			.body(out)
			.asString().getBody()
			;			
			//System.err.println(res);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}}
