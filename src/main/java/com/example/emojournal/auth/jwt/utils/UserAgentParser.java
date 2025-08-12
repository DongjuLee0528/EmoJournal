package com.example.emojournal.auth.jwt.utils;

import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public String extractBrowserVersion(String userAgent) {
        if (userAgent.contains("Edg/")) {
            return userAgent.substring(userAgent.indexOf("Edg/") + 4).split(" ")[0];
        } else if (userAgent.contains("Chrome/")) {
            return userAgent.substring(userAgent.indexOf("Chrome/") + 7).split(" ")[0];
        } else if (userAgent.contains("Firefox/")) {
            return userAgent.substring(userAgent.indexOf("Firefox/") + 8).split(" ")[0];
        } else if (userAgent.contains("Safari/") && userAgent.contains("Version/")) {
            return userAgent.substring(userAgent.indexOf("Version/") + 8).split(" ")[0];
        }
        return "Unknown";
    }

    public String extractBrowserInfo(String ua) {
        if (ua.contains("Edg/")) {
            return "Edge " + ua.substring(ua.indexOf("Edg/") + 4).split(" ")[0];
        } else if (ua.contains("Chrome/")) {
            return "Chrome " + ua.substring(ua.indexOf("Chrome/") + 7).split(" ")[0];
        } else if (ua.contains("Firefox/")) {
            return "Firefox " + ua.substring(ua.indexOf("Firefox/") + 8).split(" ")[0];
        } else if (ua.contains("Safari/") && ua.contains("Version/")) {
            return "Safari " + ua.substring(ua.indexOf("Version/") + 8).split(" ")[0];
        }
        return "Unknown";
    }


}
