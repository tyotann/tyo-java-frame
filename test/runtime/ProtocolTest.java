package runtime;

import com.ihidea.probuffer.storm.MXShop.MsgShopInfo;
import com.ihidea.probuffer.storm.MXShop.MsgShopList;

public class ProtocolTest {

	public static void main(String[] args) throws Exception {
		
		
		initProtobuf(10);

	}

	public static void initProtobuf(int cnt) {

		MsgShopList.Builder shopList = MsgShopList.newBuilder();

		for (int i = 0; i < cnt; i++) {
			MsgShopInfo.Builder shopInfo = MsgShopInfo.newBuilder();
			
			shopInfo.setAddress("");
			shopInfo.setAvgScore("100");
			shopInfo.setCanNight(1);
			shopInfo.setCanPerfect(1);
			shopInfo.setCanSOS(0);
			shopInfo.setCanWaxing(0);
			shopInfo.setCommentPositiveCnt(1);
			shopInfo.setCouponsPrice("29.30");
			shopInfo.setDiscountPrice("28.00");
			shopInfo.setDistance("10000");
			shopInfo.setId("faa44ecc-5b10-460e-8d0d-d14749ce4ef1");
			shopInfo.setImg("faa44ecc-5b10-460e-8d0d-d14749ce4ef1");
			
			shopList.addList(shopInfo);
		}

	}

}
