package com.ihidea.core.util;

import java.util.Date;

/**
 * http://mp.weixin.qq.com/s?__biz=MzA5MDE4MDMyOQ==&mid=200196710&idx=1&sn=1c455262dc9164b50d9af279b39fc689&uin=MjEzNjQ5MzMwMQ==<br>
 * http://blog.csdn.net/coolypf/article/details/8569813<br>
 * http://blog.sina.com.cn/s/blog_80a9926b0101ktoa.html<br>
 * 火星坐标系:GCJ-02 <br>
 * 地球坐标系:WGS-84 <br>
 * 百度坐标系:BD-09 <br>
 * @author TYOTANN
 */
public class MapUtils {

	private static double x_pi = (3.14159265358979324 * 3000.0 / 180.0);

	/**
	 * 火星坐标系 (GCJ-02) 到百度地图坐标系 (BD-09)的转换算法
	 * @param lat
	 * @param lon
	 */
	public static Double[] gg2bd(Double lat, Double lon) {
		double x = lon, y = lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);

		return new Double[] { z * Math.sin(theta) + 0.006, z * Math.cos(theta) + 0.0065 };
	}

	/**
	 * 百度地图坐标系 (BD-09)到火星坐标系的转换算法
	 * @param lat
	 * @param lon
	 */
	public static Double[] bd2gg(Double lat, Double lon) {
		double x = lon - 0.0065, y = lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		return new Double[] { z * Math.sin(theta), z * Math.cos(theta) };
	}

	/**
	 * 地球坐标系(WGS-84)到火星坐标系(GCJ-02)
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static Double[] wcg2gg(Double wgLat, Double wgLon) {

		double a = 6378245.0;
		double ee = 0.00669342162296594323;

		Double[] result = new Double[2];
		if (wcg2gg_outOfChina(wgLat, wgLon)) {
			result[0] = wgLat;
			result[1] = wgLon;
			return result;
		}
		double dLat = wcg2gg_transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = wcg2gg_transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);

		result[0] = wgLat + dLat;
		result[1] = wgLon + dLon;
		return result;

	}

	/**
	 * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
	 * @param lng1
	 * @param lat1
	 * @param lng2
	 * @param lat2
	 * @return
	 */
	public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	private static final double EARTH_RADIUS = 6378137;

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	private static double pi = 3.14159265358979324;

	private static boolean wcg2gg_outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	private static double wcg2gg_transformLat(double x, double y) {

		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double wcg2gg_transformLon(double x, double y) {

		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
		return ret;
	}

	public static void main(String[] args) throws Exception {

		Double[] gg = wcg2gg(31.814800, 119.989405);
		System.out.println("gg-lat:" + gg[0] + ",lon:" + gg[1]);

		// Double[] gg = bd2gg(31.820875, 119.99211);
		// System.out.println("gg-lat:" + gg[0] + ",lon:" + gg[1]);
		//
		// Double[] bd = gg2bd(31.814608872921184, 119.98566603004919);
		// System.out.println("bd-lat:" + bd[0] + ",lon:" + bd[1]);
	}

}

class Converter {
	Converter me;

	double casm_rr = 0;

	double casm_t1 = 0;

	double casm_t2 = 0;

	double casm_x1 = 0;

	double casm_y1 = 0;

	double casm_x2 = 0;

	double casm_y2 = 0;

	double casm_f = 0;

	public Converter() {
		this.me = this;
		this.casm_rr = 0;
		this.casm_t1 = 0;
		this.casm_t2 = 0;
		this.casm_x1 = 0;
		this.casm_y1 = 0;
		this.casm_x2 = 0;
		this.casm_y2 = 0;
		this.casm_f = 0;
	}

	protected double yj_sin2(double x) {
		double tt;
		double ss;
		double ff;
		double s2;
		int cc;
		ff = 0;
		if (x < 0) {
			x = -x;
			ff = 1;
		}

		cc = (int) (x / 6.28318530717959);

		tt = x - cc * 6.28318530717959;
		if (tt > 3.1415926535897932) {
			tt = tt - 3.1415926535897932;
			if (ff == 1) {
				ff = 0;
			} else if (ff == 0) {
				ff = 1;
			}
		}
		x = tt;
		ss = x;
		s2 = x;
		tt = tt * tt;
		s2 = s2 * tt;
		ss = ss - s2 * 0.166666666666667;
		s2 = s2 * tt;
		ss = ss + s2 * 8.33333333333333E-03;
		s2 = s2 * tt;
		ss = ss - s2 * 1.98412698412698E-04;
		s2 = s2 * tt;
		ss = ss + s2 * 2.75573192239859E-06;
		s2 = s2 * tt;
		ss = ss - s2 * 2.50521083854417E-08;
		if (ff == 1) {
			ss = -ss;
		}
		return ss;
	}

	protected double Transform_yj5(double x, double y) {
		double tt;
		tt = 300 + 1 * x + 2 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.sqrt(x * x));
		tt = tt + (20 * me.yj_sin2(18.849555921538764 * x) + 20 * me.yj_sin2(6.283185307179588 * x)) * 0.6667;
		tt = tt + (20 * me.yj_sin2(3.141592653589794 * x) + 40 * me.yj_sin2(1.047197551196598 * x)) * 0.6667;
		tt = tt + (150 * me.yj_sin2(0.2617993877991495 * x) + 300 * me.yj_sin2(0.1047197551196598 * x)) * 0.6667;
		return tt;
	}

	protected double Transform_yjy5(double x, double y) {
		double tt;
		tt = -100 + 2 * x + 3 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.sqrt(x * x));
		tt = tt + (20 * me.yj_sin2(18.849555921538764 * x) + 20 * me.yj_sin2(6.283185307179588 * x)) * 0.6667;
		tt = tt + (20 * me.yj_sin2(3.141592653589794 * y) + 40 * me.yj_sin2(1.047197551196598 * y)) * 0.6667;
		tt = tt + (160 * me.yj_sin2(0.2617993877991495 * y) + 320 * me.yj_sin2(0.1047197551196598 * y)) * 0.6667;
		return tt;
	}

	protected double Transform_jy5(double x, double xx) {
		double n;
		double a;
		double e;
		a = 6378245;
		e = 0.00669342;
		n = Math.sqrt(1 - e * me.yj_sin2(x * 0.0174532925199433) * me.yj_sin2(x * 0.0174532925199433));
		n = (xx * 180) / (a / n * Math.cos(x * 0.0174532925199433) * 3.1415926);
		return n;
	}

	protected double Transform_jyj5(double x, double yy) {
		double m;
		double a;
		double e;
		double mm;
		a = 6378245;
		e = 0.00669342;
		mm = 1 - e * me.yj_sin2(x * 0.0174532925199433) * me.yj_sin2(x * 0.0174532925199433);
		m = (a * (1 - e)) / (mm * Math.sqrt(mm));
		return (yy * 180) / (m * 3.1415926);
	}

	protected int r_yj() {
		int casm_a = 314159269;
		int casm_c = 453806245;
		return 0;
	}

	protected double random_yj() {
		double t;
		double casm_a = 314159269;
		double casm_c = 453806245;
		me.casm_rr = casm_a * me.casm_rr + casm_c;
		t = (int) (me.casm_rr / 2);
		me.casm_rr = me.casm_rr - t * 2;
		me.casm_rr = me.casm_rr / 2;
		return (me.casm_rr);
	}

	protected void IniCasm(double w_time, double w_lng, double w_lat) {
		double tt;
		me.casm_t1 = w_time;
		me.casm_t2 = w_time;
		tt = (int) (w_time / 0.357);
		me.casm_rr = w_time - tt * 0.357;
		if (w_time == 0)
			me.casm_rr = 0.3;
		me.casm_x1 = w_lng;
		me.casm_y1 = w_lat;
		me.casm_x2 = w_lng;
		me.casm_y2 = w_lat;
		me.casm_f = 3;
	}

	protected Double[] wgtochina_lb(int wg_flag, double wg_lng, double wg_lat, int wg_heit, int wg_week, int wg_time) {

		Double[] point = null;

		double x_add;
		double y_add;
		double h_add;
		double x_l;
		double y_l;
		double casm_v;
		double t1_t2;
		double x1_x2;
		double y1_y2;
		// Point point = null;
		if (wg_heit > 5000) {
			return point;
		}
		x_l = wg_lng;
		x_l = x_l / 3686400.0;
		y_l = wg_lat;
		y_l = y_l / 3686400.0;
		if (x_l < 72.004) {
			return point;
		}
		if (x_l > 137.8347) {
			return point;
		}
		if (y_l < 0.8293) {
			return point;
		}
		if (y_l > 55.8271) {
			return point;
		}
		if (wg_flag == 0) {
			me.IniCasm(wg_time, wg_lng, wg_lat);
			// point = new Point();
			// point.setLatitude(wg_lng);
			// point.setLongitude(wg_lat);
			return new Double[] { wg_lat, wg_lng };
		}
		me.casm_t2 = wg_time;
		t1_t2 = (me.casm_t2 - me.casm_t1) / 1000.0;
		if (t1_t2 <= 0) {
			me.casm_t1 = me.casm_t2;
			me.casm_f = me.casm_f + 1;
			me.casm_x1 = me.casm_x2;
			me.casm_f = me.casm_f + 1;
			me.casm_y1 = me.casm_y2;
			me.casm_f = me.casm_f + 1;
		} else {
			if (t1_t2 > 120) {
				if (me.casm_f == 3) {
					me.casm_f = 0;
					me.casm_x2 = wg_lng;
					me.casm_y2 = wg_lat;
					x1_x2 = me.casm_x2 - me.casm_x1;
					y1_y2 = me.casm_y2 - me.casm_y1;
					casm_v = Math.sqrt(x1_x2 * x1_x2 + y1_y2 * y1_y2) / t1_t2;
					if (casm_v > 3185) {
						return (point);
					}
				}
				me.casm_t1 = me.casm_t2;
				me.casm_f = me.casm_f + 1;
				me.casm_x1 = me.casm_x2;
				me.casm_f = me.casm_f + 1;
				me.casm_y1 = me.casm_y2;
				me.casm_f = me.casm_f + 1;
			}
		}
		x_add = me.Transform_yj5(x_l - 105, y_l - 35);
		y_add = me.Transform_yjy5(x_l - 105, y_l - 35);
		h_add = wg_heit;
		x_add = x_add + h_add * 0.001 + me.yj_sin2(wg_time * 0.0174532925199433) + me.random_yj();
		y_add = y_add + h_add * 0.001 + me.yj_sin2(wg_time * 0.0174532925199433) + me.random_yj();
		// point = new Point();
		// point.setX(((x_l + me.Transform_jy5(y_l, x_add)) * 3686400));
		// point.setY(((y_l + me.Transform_jyj5(y_l, y_add)) * 3686400));
		return new Double[] { ((x_l + me.Transform_jy5(y_l, x_add)) * 3686400), ((y_l + me.Transform_jyj5(y_l, y_add)) * 3686400) };
	}

	protected boolean isValid(long validdays) {
		// long standand = 1253525356;
		long h = 3600;
		Date currentTime = new Date();
		if (currentTime.getTime() / 1000 - 1253525356 >= validdays * 24 * h) {
			return false;
		} else {
			return true;
		}
	}

	public Double[] getEncryPoint(double x, double y) {
		Double[] point = null;
		double x1, tempx;
		double y1, tempy;
		x1 = x * 3686400.0;
		y1 = y * 3686400.0;
		double gpsWeek = 0;
		double gpsWeekTime = 0;
		double gpsHeight = 0;

		point = me.wgtochina_lb(1, (int) (x1), (int) (y1), (int) (gpsHeight), (int) (gpsWeek), (int) (gpsWeekTime));
		tempx = point[0];
		tempy = point[1];
		tempx = tempx / 3686400.0;
		tempy = tempy / 3686400.0;
		// point = new Point();
		// point.setX(tempx);
		// point.setY(tempy);
		return new Double[] { tempx, tempy };
	}

	protected String getEncryCoord(String coord, boolean flag) {
		if (flag) {

			double x = Double.parseDouble(coord.split(",")[0]);
			double y = Double.parseDouble(coord.split(",")[1]);
			Double[] point = null;
			double x1, tempx;
			double y1, tempy;
			x1 = x * 3686400.0;
			y1 = y * 3686400.0;
			int gpsWeek = 0;
			int gpsWeekTime = 0;
			int gpsHeight = 0;
			point = me.wgtochina_lb(1, (int) (x1), (int) (y1), (int) (gpsHeight), (int) (gpsWeek), (int) (gpsWeekTime));
			tempx = point[0];
			tempy = point[1];
			tempx = tempx / 3686400.0;
			tempy = tempy / 3686400.0;
			return tempx + "," + tempy;
		} else {
			return "";
		}
	}
}
