package com.ihidea.component.model;

import com.ihidea.core.base.CoreEntity;

import java.math.BigDecimal;

public class CptExportCfgKey extends CoreEntity {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HFMIS_GUIYANG.CPT_EXPORT_CFG.APP_ID
     *
     * @mbggenerated Fri Sep 14 16:05:34 CST 2012
     */
    private String appId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HFMIS_GUIYANG.CPT_EXPORT_CFG.ID
     *
     * @mbggenerated Fri Sep 14 16:05:34 CST 2012
     */
    private String id;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HFMIS_GUIYANG.CPT_EXPORT_CFG.APP_ID
     *
     * @return the value of HFMIS_GUIYANG.CPT_EXPORT_CFG.APP_ID
     *
     * @mbggenerated Fri Sep 14 16:05:34 CST 2012
     */
    public String getAppId() {
        return appId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HFMIS_GUIYANG.CPT_EXPORT_CFG.APP_ID
     *
     * @param appId the value for HFMIS_GUIYANG.CPT_EXPORT_CFG.APP_ID
     *
     * @mbggenerated Fri Sep 14 16:05:34 CST 2012
     */
    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HFMIS_GUIYANG.CPT_EXPORT_CFG.ID
     *
     * @return the value of HFMIS_GUIYANG.CPT_EXPORT_CFG.ID
     *
     * @mbggenerated Fri Sep 14 16:05:34 CST 2012
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HFMIS_GUIYANG.CPT_EXPORT_CFG.ID
     *
     * @param id the value for HFMIS_GUIYANG.CPT_EXPORT_CFG.ID
     *
     * @mbggenerated Fri Sep 14 16:05:34 CST 2012
     */
    public void setId(String id) {
        this.id = id;
    }
}