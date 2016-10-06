package runtime.model;

import java.math.BigDecimal;
import java.util.Date;

public class ShopInfo {

	private String province;

	private String city;

	private String img;

	private String name;

	private String no;

	private String address;

	private String tel;

	private String license;

	private String measure;

	private String lng;

	private String lat;

	private BigDecimal avgScore;

	private String loginPwd;

	private String bossName;

	private String bossId;

	private String bossPhone;

	private String payBank;

	private BigDecimal price;

	private BigDecimal discountPrice;

	private String payAccount;

	private BigDecimal isEnable;

	private BigDecimal status;

	private Date orderId;

	private String createAccount;

	private Date createTime;

	private String modifyAccount;

	private Date modifyTime;

	private BigDecimal commentPositiveCnt;

	private BigDecimal commentNeutralCnt;

	private BigDecimal commentNegativeCnt;

	private BigDecimal totalOrderCnt;

	private String imgMain;

	private String orderNotifyPhones;

	private String abbreviation;

	private String mapGcj02;

	private String mapBd09;

	private String cityAreaId;

	private String businessTime;

	private BigDecimal canNight;

	private BigDecimal canPerfect;

	private BigDecimal canWaxing;

	private BigDecimal canSos;

	private String info;

	// 好评率
	private BigDecimal positiveRate;

	private String distance;// 门店距离

	private String orderType;// 排序方式(1距离最近，2价格最低，3评价最好)

	private String accountId;// 用户编号，收藏查询时使用

	private BigDecimal isFavorites;// 用户是否收藏标记

	private String oId;// 通过订单号获得门店信息

	private String billIds;// 导出对账账单时使用

	private String paramStr;// 手机端搜索字符串

	private String provinceCode;// 省份code

	private String cityCode;// 市级code

	private String agentId;// 后台管理用户id，用于查询时区分负责地区

	private BigDecimal isBusiness;// 是否营业中

	private String areaCode;// 区域或商圈代码

	private BigDecimal isRecently;// 是否查询最近洗过门店

	private BigDecimal imgCnt;// 图片个数

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public BigDecimal getIsBusiness() {
		return isBusiness;
	}

	public void setIsBusiness(BigDecimal isBusiness) {
		this.isBusiness = isBusiness;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void setPositiveRate(BigDecimal positiveRate) {
		this.positiveRate = positiveRate;
	}

	public BigDecimal getIsFavorites() {
		return isFavorites;
	}

	public void setIsFavorites(BigDecimal isFavorites) {
		this.isFavorites = isFavorites;
	}

	public String getOId() {
		return oId;
	}

	public void setOId(String oId) {
		this.oId = oId;
	}

	public String getBillIds() {
		return billIds;
	}

	public void setBillIds(String billIds) {
		this.billIds = billIds;
	}

	public String getParamStr() {
		return paramStr;
	}

	public void setParamStr(String paramStr) {
		this.paramStr = paramStr;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public BigDecimal getIsRecently() {
		return isRecently;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public BigDecimal getAvgScore() {
		return avgScore;
	}

	public void setAvgScore(BigDecimal avgScore) {
		this.avgScore = avgScore;
	}

	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}

	public String getBossName() {
		return bossName;
	}

	public void setBossName(String bossName) {
		this.bossName = bossName;
	}

	public String getBossId() {
		return bossId;
	}

	public void setBossId(String bossId) {
		this.bossId = bossId;
	}

	public String getBossPhone() {
		return bossPhone;
	}

	public void setBossPhone(String bossPhone) {
		this.bossPhone = bossPhone;
	}

	public String getPayBank() {
		return payBank;
	}

	public void setPayBank(String payBank) {
		this.payBank = payBank;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(BigDecimal discountPrice) {
		this.discountPrice = discountPrice;
	}

	public String getPayAccount() {
		return payAccount;
	}

	public void setPayAccount(String payAccount) {
		this.payAccount = payAccount;
	}

	public BigDecimal getIsEnable() {
		return isEnable;
	}

	public void setIsEnable(BigDecimal isEnable) {
		this.isEnable = isEnable;
	}

	public BigDecimal getStatus() {
		return status;
	}

	public void setStatus(BigDecimal status) {
		this.status = status;
	}

	public Date getOrderId() {
		return orderId;
	}

	public void setOrderId(Date orderId) {
		this.orderId = orderId;
	}

	public String getCreateAccount() {
		return createAccount;
	}

	public void setCreateAccount(String createAccount) {
		this.createAccount = createAccount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getModifyAccount() {
		return modifyAccount;
	}

	public void setModifyAccount(String modifyAccount) {
		this.modifyAccount = modifyAccount;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public BigDecimal getCommentPositiveCnt() {
		return commentPositiveCnt;
	}

	public void setCommentPositiveCnt(BigDecimal commentPositiveCnt) {
		this.commentPositiveCnt = commentPositiveCnt;
	}

	public BigDecimal getCommentNeutralCnt() {
		return commentNeutralCnt;
	}

	public void setCommentNeutralCnt(BigDecimal commentNeutralCnt) {
		this.commentNeutralCnt = commentNeutralCnt;
	}

	public BigDecimal getCommentNegativeCnt() {
		return commentNegativeCnt;
	}

	public void setCommentNegativeCnt(BigDecimal commentNegativeCnt) {
		this.commentNegativeCnt = commentNegativeCnt;
	}

	public BigDecimal getTotalOrderCnt() {
		return totalOrderCnt;
	}

	public void setTotalOrderCnt(BigDecimal totalOrderCnt) {
		this.totalOrderCnt = totalOrderCnt;
	}

	public String getImgMain() {
		return imgMain;
	}

	public void setImgMain(String imgMain) {
		this.imgMain = imgMain;
	}

	public String getOrderNotifyPhones() {
		return orderNotifyPhones;
	}

	public void setOrderNotifyPhones(String orderNotifyPhones) {
		this.orderNotifyPhones = orderNotifyPhones;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getMapGcj02() {
		return mapGcj02;
	}

	public void setMapGcj02(String mapGcj02) {
		this.mapGcj02 = mapGcj02;
	}

	public String getMapBd09() {
		return mapBd09;
	}

	public void setMapBd09(String mapBd09) {
		this.mapBd09 = mapBd09;
	}

	public String getCityAreaId() {
		return cityAreaId;
	}

	public void setCityAreaId(String cityAreaId) {
		this.cityAreaId = cityAreaId;
	}

	public String getBusinessTime() {
		return businessTime;
	}

	public void setBusinessTime(String businessTime) {
		this.businessTime = businessTime;
	}

	public BigDecimal getCanNight() {
		return canNight;
	}

	public void setCanNight(BigDecimal canNight) {
		this.canNight = canNight;
	}

	public BigDecimal getCanPerfect() {
		return canPerfect;
	}

	public void setCanPerfect(BigDecimal canPerfect) {
		this.canPerfect = canPerfect;
	}

	public BigDecimal getCanWaxing() {
		return canWaxing;
	}

	public void setCanWaxing(BigDecimal canWaxing) {
		this.canWaxing = canWaxing;
	}

	public BigDecimal getCanSos() {
		return canSos;
	}

	public void setCanSos(BigDecimal canSos) {
		this.canSos = canSos;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getoId() {
		return oId;
	}

	public void setoId(String oId) {
		this.oId = oId;
	}

	public BigDecimal getPositiveRate() {
		return positiveRate;
	}

	public void setIsRecently(BigDecimal isRecently) {
		this.isRecently = isRecently;
	}

	public BigDecimal getImgCnt() {
		return imgCnt;
	}

	public void setImgCnt(BigDecimal imgCnt) {
		this.imgCnt = imgCnt;
	}
}
