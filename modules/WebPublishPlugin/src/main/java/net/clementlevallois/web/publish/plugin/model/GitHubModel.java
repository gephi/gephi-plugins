/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.web.publish.plugin.model;

/**
 *
 * @author LEVALLOIS
 */
public class GitHubModel {
    
    private String accessToken;
    private String deviceCode;

    public String getAccessToken() {
        return accessToken;
    }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

}
