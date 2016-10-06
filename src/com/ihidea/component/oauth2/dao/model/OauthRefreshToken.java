package com.ihidea.component.oauth2.dao.model;

import com.ihidea.core.base.CoreEntity;

import java.util.Date;

public class OauthRefreshToken extends CoreEntity {
    private String tokenId;

    private String appKey;

    private String openId;

    private Date invalidDate;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId == null ? null : tokenId.trim();
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey == null ? null : appKey.trim();
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    public Date getInvalidDate() {
        return invalidDate;
    }

    public void setInvalidDate(Date invalidDate) {
        this.invalidDate = invalidDate;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        OauthRefreshToken other = (OauthRefreshToken) that;
        return (this.getTokenId() == null ? other.getTokenId() == null : this.getTokenId().equals(other.getTokenId()))
            && (this.getAppKey() == null ? other.getAppKey() == null : this.getAppKey().equals(other.getAppKey()))
            && (this.getOpenId() == null ? other.getOpenId() == null : this.getOpenId().equals(other.getOpenId()))
            && (this.getInvalidDate() == null ? other.getInvalidDate() == null : this.getInvalidDate().equals(other.getInvalidDate()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTokenId() == null) ? 0 : getTokenId().hashCode());
        result = prime * result + ((getAppKey() == null) ? 0 : getAppKey().hashCode());
        result = prime * result + ((getOpenId() == null) ? 0 : getOpenId().hashCode());
        result = prime * result + ((getInvalidDate() == null) ? 0 : getInvalidDate().hashCode());
        return result;
    }
}