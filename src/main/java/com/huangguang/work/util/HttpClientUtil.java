package com.huangguang.work.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.*;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.LineParser;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;


public class HttpClientUtil {
	public static final String DEFAULT_CHARSET_ENCODING = "UTF-8";

	private static CookieStore cookieStore = new BasicCookieStore();

	public static CookieStore getCookieStore() {
		return cookieStore;
	}

	public static void setCookieStore(CookieStore cookieStore) {
		HttpClientUtil.cookieStore = cookieStore;
	}

	public static CloseableHttpClient createHttpsClient() {
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");

			// set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			            public X509Certificate[] getAcceptedIssuers() {
			                    return null;
			            }

			            public void checkClientTrusted(X509Certificate[] certs,
			                            String authType) {
			            }

			            public void checkServerTrusted(X509Certificate[] certs,
			                            String authType) {
			            }
			} }, new SecureRandom());
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
			return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}

	public static String httpGet(URI uri, String charsetEncoding) {
		HttpClient httpClient = HttpClients.createDefault();
		return doHttpGet(uri, httpClient, charsetEncoding);
	}

	public static String httpGet(URI uri) {
		return httpGet(uri, "UTF-8");
	}

	public static String httpGet(String url, Map<String, String> params, String charsetEncoding) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
		}
		String paramString = URLEncodedUtils.format(nvps, charsetEncoding);
		return httpGet(URI.create(url + "?" + paramString), charsetEncoding);
	}

	public static String httpGet(String url) {
		return httpGet(url, new HashMap<>());
	}
	public static String httpGet(String url, Map<String, String> params) {
		return httpGet(url, params, "UTF-8");
	}

	public static String sslHttpGet(URI uri, String charsetEncoding) {
		HttpClient httpClient = createHttpsClient();
		return doHttpGet(uri, httpClient, charsetEncoding);
	}

	public static String sslHttpGet(URI uri) {
		return sslHttpGet(uri, "UTF-8");
	}

	public static String sslHttpGet(String url, Map<String, String> params, String charsetEncoding) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
		}
		String paramString = URLEncodedUtils.format(nvps, charsetEncoding);
		return sslHttpGet(URI.create(url + "?" + paramString), charsetEncoding);
	}

	public static String sslHttpGet(String url, Map<String, String> params) {
		return sslHttpGet(url, params, "UTF-8");
	}

	
	public static String httpPost(URI uri, Map<String, String> params, String charsetEncoding) {
		HttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
		}
		return doHttpPost(uri, httpClient, nvps, charsetEncoding);
	}
	
	
	public static String httpPostProxy(URI uri, Map<String, String> params, String charsetEncoding) {
		HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
		      public HttpMessageParser<HttpResponse> create(SessionInputBuffer buffer, MessageConstraints constraints) {
		                LineParser lineParser = new BasicLineParser() {
		                    public Header parseHeader(final CharArrayBuffer buffer) {
		                        try {
		                            return super.parseHeader(buffer);
		                        } catch (ParseException ex) {
		                            return new BasicHeader(buffer.toString(), null);
		                        }
		                    }
		                };
		                
		                
		                return new DefaultHttpResponseParser(buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {
		                    protected boolean reject(final CharArrayBuffer line, int count) {
		                        return false;
		                    }

		                };
		            }

		        }; 
		HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();
		HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
              requestWriterFactory, responseParserFactory);
		SSLContext sslcontext = SSLContexts.createSystemDefault();
	    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
	            .register("http", PlainConnectionSocketFactory.INSTANCE)
	            .register("https", new SSLConnectionSocketFactory(sslcontext))
	            .build();
	    DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
          public InetAddress[] resolve(final String host) throws UnknownHostException {
              if (host.equalsIgnoreCase("myhost")) {
                  return new InetAddress[] { InetAddress.getByAddress(new byte[] {127, 0, 0, 1}) };
              } else {
                  return super.resolve(host);
              }
          }
      };
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
              socketFactoryRegistry, connFactory, dnsResolver);
      //设置连接池
      connManager.setMaxTotal(1500);
     
      connManager.setDefaultMaxPerRoute(400);
		CloseableHttpClient httpclient = HttpClients.custom()
              .setConnectionManager(connManager)
              .setProxy(new HttpHost("10.26.98.138", 3128))//设置代理,如果没有关闭 则关闭代理
              .build();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
		}
		return doHttpPost(uri, httpclient, nvps, charsetEncoding);
	}

	public static String httpPost(URI uri, Map<String, String> params) {
		return httpPost(uri, params, "UTF-8");
	}

	public static String httpPost(String url, Map<String, String> params, String charsetEncoding) {
		URI uri = URI.create(url);
		return httpPost(uri, params, charsetEncoding);
	}
	
	public static String httpPost(String url, Map<String, String> params, String charsetEncoding,boolean proxy) {
		URI uri = URI.create(url);
		if(proxy){
			return httpPostProxy(uri, params, charsetEncoding);
		}
		return httpPost(uri, params, charsetEncoding);
	}

	
	public static String httpPostProxy(String url, Map<String, String> params, String charsetEncoding) {
		URI uri = URI.create(url);
		return httpPost(uri, params, charsetEncoding);
	}
	
	
	public static String httpPost(String url, Map<String, String> params) {
		return httpPost(url, params, "UTF-8");
	}

	public static String sslHttpPost(URI uri, Map<String, String> params, String charsetEncoding) {
		HttpClient httpClient = createHttpsClient();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
		}
		return doHttpPost(uri, httpClient, nvps, charsetEncoding);
	}

	public static String sslHttpPost(String url, Map<String, String> params, String charsetEncoding) {
		URI uri = URI.create(url);
		return sslHttpPost(uri, params, charsetEncoding);
	}

	public static String sslHttpPost(String url, Map<String, String> params) {
		return sslHttpPost(url, params, "UTF-8");
	}

	public static String jsonPost(URI uri, String json, String charsetEncoding) {
		HttpClient httpClient = HttpClients.createDefault();
		return doJsonPost(uri, httpClient, json, charsetEncoding);
	}

	public static String jsonPost(URI uri, String json) {
		return jsonPost(uri, json, "UTF-8");
	}

	public static String jsonPost(String url, String json, String charsetEncoding) {
		URI uri = URI.create(url);
		return jsonPost(uri, json, charsetEncoding);
	}

	public static String jsonPost(String url, String json) {
		return jsonPost(url, json, "UTF-8");
	}

	public static String sslJsonPost(URI uri, String json, String charsetEncoding) {
		HttpClient httpClient = createHttpsClient();
		return doJsonPost(uri, httpClient, json, charsetEncoding);
	}

	public static String sslJsonPost(URI uri, String json) {
		return sslJsonPost(uri, json, "UTF-8");
	}

	public static String sslJsonPost(String url, String json, String charsetEncoding) {
		URI uri = URI.create(url);
		return sslJsonPost(uri, json, charsetEncoding);
	}

	public static String sslJsonPost(String url, String json) {
		return sslJsonPost(url, json, "UTF-8");
	}

	public static String xmlPost(URI uri, String xml, String charsetEncoding) {
		HttpClient httpClient = HttpClients.createDefault();
		return doXmlPost(uri, httpClient, xml, charsetEncoding);
	}

	public static String xmlPost(URI uri, String xml) {
		return jsonPost(uri, xml, "UTF-8");
	}

	public static String xmlPost(String url, String xml, String charsetEncoding) {
		URI uri = URI.create(url);
		return jsonPost(uri, xml, charsetEncoding);
	}

	public static String xmlPost(String url, String xml) {
		return jsonPost(url, xml, "UTF-8");
	}

	private static String doHttpGet(URI uri, HttpClient httpClient, String charsetEncoding) {
		HttpGet httpGet = new HttpGet(uri);
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet, context);
			cookieStore = context.getCookieStore();
			InputStream in = httpResponse.getEntity().getContent();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetEncoding));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				sb.append(line);
			}
			in.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	
	
private static String doHttpPost(URI uri, CloseableHttpClient httpClient, List<NameValuePair> nvps, String charsetEncoding) {
		HttpPost httpPost = new HttpPost(uri);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, charsetEncoding));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			InputStream in = httpResponse.getEntity().getContent();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetEncoding));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				sb.append(line);
			}
			in.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
	}


	private static String doHttpPost(URI uri, HttpClient httpClient, List<NameValuePair> nvps, String charsetEncoding) {
		HttpPost httpPost = new HttpPost(uri);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, charsetEncoding));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			InputStream in = httpResponse.getEntity().getContent();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetEncoding));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				sb.append(line);
			}
			in.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	private static String doJsonPost(URI uri, HttpClient httpClient, String json, String charsetEncoding) {
		HttpPost httpPost = new HttpPost(uri);
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		try {
			StringEntity stringEntity = new StringEntity(json, charsetEncoding);
			stringEntity.setContentType("application/json");
			httpPost.setEntity(stringEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost, context);
			cookieStore = context.getCookieStore();
			InputStream in = httpResponse.getEntity().getContent();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetEncoding));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				sb.append(line);
			}
			in.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	private static String doXmlPost(URI uri, HttpClient httpClient, String json, String charsetEncoding) {
		HttpPost httpPost = new HttpPost(uri);
		try {
			StringEntity stringEntity = new StringEntity(json, charsetEncoding);
			stringEntity.setContentType("application/xml");
			httpPost.setEntity(stringEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			InputStream in = httpResponse.getEntity().getContent();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetEncoding));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				sb.append(line);
			}
			in.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static String getInfoFromRequest(HttpServletRequest request) throws UnsupportedEncodingException {
		StringBuffer info = new StringBuffer();
		InputStream in = null ;
		try {
			in = request.getInputStream();
			BufferedInputStream buf = new BufferedInputStream(in);
			byte[] buffer = new byte[1024];
			int iRead;
			while ((iRead = buf.read(buffer)) != -1) {
				info.append(new String(buffer, 0, iRead, "UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//logger.info("接受信息="+info.toString());
		return URLDecoder.decode(info.toString(),"UTF-8");
	}
	
	
	
	/**
	 * @category 获取request参数
	 * @param request
	 * @return
	 */
	public  static Map<String, String> request2Map(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> enums = request.getParameterNames();
		while (enums.hasMoreElements()) {
			String name = enums.nextElement();
			String value = request.getParameter(name);
			if (request.getParameterValues(name) != null) {
				value = StringUtils.join(request.getParameterValues(name), ",");
			}
			map.put(name, value);
		}
		return map;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		//CryptoUtils desCrypto = new CryptoUtils();
		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("userID", "admin");
		paramMap.put("password", "founder");
		paramMap.put("version", "1.0");
		paramMap.put("ismi", "123");
		paramMap.put("serviceName", "userLogin");

		final HashMap<String, String> map = new HashMap<String, String>();
		//CryptoUtils.encryptParams(map, paramMap);
		map.put("access_token","g1-XgBh8GQVIpIj3u_GYj8d8I4hRbelGuPqVtQ-IkQvKuHHI-MrpAKX1lxnbAYVaKjmXBtteVHV_FmUDlPm_VtNxDo3JraayKcWjFoE76LQ");
		map.put("openid", "os712wgj6-95tMtV6Td4BLZsIEGg");
		
		String result = HttpClientUtil.sslHttpGet("https://www.baidu.com/?tn=91485939_hao_pg", map);
		System.out.println(result);
//		Map<String,Object> resultMap = JsonUtils.fromJson(result, Map.class);
//		System.out.println(resultMap.get("errcode"));
//		if(!"0".equals(resultMap.get("errcode").toString())){
//			System.err.println(resultMap.get("errcode"));
//		}
//		
//		for (int i = 0; i < 1000; i++) {
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					String result = httpPost("http://127.0.0.1:8081/lucky/openLuckyMoney.json?id=73c08160f38e44c0a382b68360e9c5c3&token=Bg4wNwKmrr9XfMUn5vWxidPf7VCTRzlRKCLFpK461i2oaKUeTjmU25170raa5LlR2ahv3jGJC4n2DnQbD-zUY6_kTvx3h-siTbsuUozbnmzcqNJzhL1QdCPgNDcVmIr4", map, "UTF-8");
//					System.out.println(result);
//				}
//			}).start();
//		}
	}
//
//	public static void encryptParams(Map<String, String> target, HashMap<String, String> source) {
//		DESPlus desCrypto = new DESPlus();
//		if (source != null) {
//			for (String keyset : source.keySet()) {
//				target.put(keyset, desCrypto.encrypt((String) source.get(keyset)));
//			}
//		}
//	}
	
	
	
}