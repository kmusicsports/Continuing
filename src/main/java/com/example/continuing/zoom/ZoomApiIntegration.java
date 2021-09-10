package com.example.continuing.zoom;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.springframework.util.StringUtils;

import com.example.continuing.dto.MeetingDto;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class ZoomApiIntegration {

	// App credentials
	private static String CLIENT_ID = ZoomDetails.getClientId();
	private static String CLIENT_SECRET = ZoomDetails.getClientSecret();
	
	// API URL
	private static String PROFILE_API_URL = ZoomDetails.getProfileApiUrl();
	private static String DELETE_MEETING_API_URL = ZoomDetails.getDELETE_MEETING_API_URL();
	
	// Redirect URI
	private static String REDIRECT_URI = ZoomDetails.getRedirectUri();
	private static String REDIRECT_URI_CREATE = ZoomDetails.getRedirectUriCreate();
	private static String REDIRECT_URI_DELETE = ZoomDetails.getRedirectUriDelete();
	private static String REDIRECT_URI_UPDATE = ZoomDetails.getRedirectUriUpdate();
	
	private static String SESSION_STATE = ZoomDetails.getSessionState();
	
	// OAuth認証URL生成
    public String getAuthorizationUrl(HttpSession session) {
        // セッション検証のために乱数を生成
        String state = generateRandomString();
        // 生成された乱数値をsessionに保存
        setSession(session, state);        
        
        // 何のAPIかを判断する
        String zoom = ZoomDetails.getZOOM_STATE();        
        String url = null;
        if(zoom.equals("zoom")){
        	url = REDIRECT_URI;
        }else if (zoom.equals("zoom_create")) {
        	url = REDIRECT_URI_CREATE;
		}else if (zoom.equals("zoom_delete")) {
			url = REDIRECT_URI_DELETE;
		}else if (zoom.equals("zoom_update")) {
			url = REDIRECT_URI_UPDATE;
		}

        // Scribeで提供する認証URL生成機能を利用して認証URLを生成
        System.out.println("url:" + url);
        OAuth20Service oauthService = new ServiceBuilder()                                                   
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(url)
                .state(state) // 上で作成した乱数値を認証URL生成時有効
                .build(ZoomDetails.instance());
        
        return oauthService.getAuthorizationUrl();
    }
    
    // AccessToken取得
    public OAuth2AccessToken getAccessToken(HttpSession session, String code, String state) throws IOException{
    	System.out.println("Zoom getAccessToken Method");
        // callbackに伝達された細線検証用乱数値とのセッションに保存されている値が一致することを確認
    	String zoom = ZoomDetails.getZOOM_STATE();
        String url = null;
    	
        if(zoom.equals("zoom")){
         	url = REDIRECT_URI;
        }else if (zoom.equals("zoom_create")) {
         	url = REDIRECT_URI_CREATE;
 		}else if (zoom.equals("zoom_delete")) {
			url = REDIRECT_URI_DELETE;
		}else if (zoom.equals("zoom_update")) {
			url = REDIRECT_URI_UPDATE;
		}
        System.out.println("url: " +url);
        String sessionState = getSession(session);
        if(StringUtils.pathEquals(sessionState, state)){
        	
            OAuth20Service oauthService = new ServiceBuilder()
                    .apiKey(CLIENT_ID)
                    .apiSecret(CLIENT_SECRET)
                    .callback(url)
                    .state(state)
                    .build(ZoomDetails.instance());
            // Scribeで提供されるAccessToken取得機能にAccess Tokenを取得
            OAuth2AccessToken accessToken = oauthService.getAccessToken(code);
            return accessToken;
        }
        return null;
    }
    
    
    // セッションの妥当性検証のための乱数発生器
    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }
    
    // HttpSessionにデータを保存
    private void setSession(HttpSession session,String state){
        session.setAttribute(SESSION_STATE, state);     
    }
    
    // HttpSessionからデータをインポート 
    private String getSession(HttpSession session){
        return (String) session.getAttribute(SESSION_STATE);
    }
   
    
    /* * * * * * * * * * * * * * * * * * * * *
     * * * * * * * * Call API * * * * * * * * 
     * * * * * * * * * * * * * * * * * * * * */
    
    	
    // 会議の追加
    public String createMeeting(OAuth2AccessToken oauthToken, MeetingDto meetingDto) throws IOException{
    	 System.out.println("-会議追加サービス");
    	 
        OAuth20Service oauthService =new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI).build(ZoomDetails.instance());
        
        OAuthRequest request = new OAuthRequest(Verb.POST, PROFILE_API_URL, oauthService);
        JSONObject jsonOb = new JSONObject();
        
        jsonOb.put("topic", meetingDto.getTopic());
        jsonOb.put("type", meetingDto.getType());
        jsonOb.put("start_time", meetingDto.getStartTime());
        jsonOb.put("duration", meetingDto.getDuration());
        jsonOb.put("password", meetingDto.getPassword());
        jsonOb.put("agenda", meetingDto.getAgenda());
        
        request.addHeader("Content-Type", "application/json;charset=UTF-8");

        System.out.println("json output: " + jsonOb.toString());		
        
        request.addPayload(jsonOb.toString());
        oauthService.signRequest(oauthToken, request);
        Response response = request.send();
        return response.getBody();
    }
    
    // 会議の削除
    public void deleteMeeting(OAuth2AccessToken oauthToken, String meetingId) throws IOException{
    	 System.out.println("-会議削除サービス");
    	 System.out.println("API URL: " + DELETE_MEETING_API_URL + meetingId);
        OAuth20Service oauthService =new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI).build(ZoomDetails.instance());
        OAuthRequest request = new OAuthRequest(Verb.DELETE, DELETE_MEETING_API_URL+ meetingId, oauthService);
        
        request.addHeader("Content-Type", "application/json;charset=UTF-8");
        JSONObject jsonOb = new JSONObject();
        System.out.println("json output: " + jsonOb.toString());		
        
        request.addPayload(jsonOb.toString());
        oauthService.signRequest(oauthToken, request);
        request.send();
    }
    
}
