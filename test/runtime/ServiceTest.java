package runtime;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ihidea.component.cache.redis.RedisUtils;
import com.ihidea.core.util.StringUtilsEx;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/spring/applicationContext-redis.xml" })
public class ServiceTest {

	public ServiceTest() {
		super();
	}

	@Autowired
	private RedisUtils redisUtils;

	@Test
	// @Transactional
	// @Rollback(false)
	public void test() throws Exception {

		long start = System.currentTimeMillis();

		// workFlowService.endProcess("123");
		
		redisUtils.remove("aaaa");

//		String key = "cache:" + StringUtilsEx.getUUID();
//
//		redisUtils.put(key, FileUtils.readFileToByteArray(new File("D:/111111111111111/3b076e5f-af8a-4076-ab63-0473c8f4746e.PNG")), 1);
//
//		// System.out.println(System.currentTimeMillis());
//
//		for (int i = 0; i < 100; i++) {
//			redisUtils.get(key, byte[].class);
//			// FileUtils.writeByteArrayToFile(new File("D:/111111111111111/xx2.PNG"), redisUtils.get(key, byte[].class));
//		}
//
//		System.out.println(System.currentTimeMillis() - start);
//
//		redisUtils.remove("test");

	}
}
