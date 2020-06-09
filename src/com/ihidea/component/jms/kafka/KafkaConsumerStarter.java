package com.ihidea.component.jms.kafka;

import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.ClassUtilsEx;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaConsumerStarter {

    private final static Logger logger = LoggerFactory.getLogger(KafkaConsumerStarter.class);

    protected static Map<String, Method> consumerMethodMap = new ConcurrentHashMap<String, Method>();

    private static KafkaConsumerRunner[] runnables ;

    private synchronized static void initConsumerMethod() throws Exception{

        Map<String, Object> kafkaConsumerClz = SpringContextLoader.getBeansWithAnnotation(KafkaConsumerHandler.class);

        if(consumerMethodMap.isEmpty()) {

            if (kafkaConsumerClz != null) {

                for (Object clzObj : kafkaConsumerClz.values()) {

                    List<Method> methodList = ClassUtilsEx.getClassMethodByAnnotation(clzObj.getClass(), KafkaConsumerHandlerMethod.class);

                    for (Method method : methodList) {

                        KafkaConsumerHandlerMethod methodAnno = method.getAnnotation(KafkaConsumerHandlerMethod.class);

                        String topicName = methodAnno.topic();

                        if (StringUtils.isNotBlank(topicName)) {

                            if (consumerMethodMap.containsKey(topicName)) {
                                throw new ServiceException("Kafka Topic:" + topicName + "有重复定义,请检查代码!");
                            }

                            consumerMethodMap.put(topicName, method);
                        }
                    }
                }
            }
        }

    }

    /**
     * 初始化消费者线程
     * @param brokerAddress         broker地址,逗号分隔
     * @param consumerGroupName     consumerGroupName
     * @param sessionTimeOutMs      session超时时间，默认30s
     * @param maxPollRecords        每次拉取消息条数，默认30条，请务必确保30秒内30条一定能消费完，否则会触发kafka broker rebalance，引发性能问题
     * @param consumerThreadNum     consumer实例个数，既consumer线程数，默认为8。一个consumer对于一个或多个分区，阿里云默认一个topic创建的分区数为6的倍数。
     *                              因此consumerThreadNum建议也设置为6的倍数，但最好不要超过24。
     */
    public static synchronized void init(String brokerAddress, String consumerGroupName, int sessionTimeOutMs, int maxPollRecords, int consumerThreadNum, Class t) throws Exception {

        initConsumerMethod();

        // 订阅的topic集合不为空才创建消费线程
        if(!consumerMethodMap.isEmpty()) {

            if (sessionTimeOutMs <= 0) {
                sessionTimeOutMs = 30000;
            }
            if (maxPollRecords <= 0) {
                maxPollRecords = 30;
            }
            if (consumerThreadNum <= 0) {
                consumerThreadNum = 6;
            }

            runnables = new KafkaConsumerRunner[consumerThreadNum];

            Properties props = new Properties();
            //设置接入点，请通过控制台获取对应 Topic 的接入点
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
            //默认值为 30000 ms，可根据自己业务场景调整此值，建议取值不要太小，防止在超时时间内没有发送心跳导致消费者再均衡
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeOutMs);
            //每次 poll 的最大数量
            //注意该值不要改得太大，如果 poll 太多数据，而不能在下次 poll 之前消费完，则会触发一次负载均衡，产生卡顿
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
            //消息的反序列化方式
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            //当前消费实例所属的 Consumer Group，请在控制台创建后填写
            //属于同一个 Consumer Group 的消费实例，会负载消费消息
            props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupName);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            // 不允许自动提交，全部手动提交
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            if(t == null){
                t = KafkaConsumerRunner.class;
            } else if(t.getSuperclass() != KafkaConsumerRunner.class){
                throw new ServiceException("非法的自定义KafkaConsumerRunner类型");
            }
            Constructor constructor = t.getConstructor(KafkaConsumer.class);
            //循环构造消息对象，即生成consumerThreadNum个消费实例
            for (int i = 0; i < consumerThreadNum; i++) {
                final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
                //设置  Consumer Group 订阅的 Topic，可订阅多个 Topic。如果 GROUP_ID_CONFIG 相同，那建议订阅的 Topic 设置也相同
                consumer.subscribe(consumerMethodMap.keySet());

                KafkaConsumerRunner runnable = (KafkaConsumerRunner)constructor.newInstance(consumer);

                runnables[i]  = runnable;

                new Thread(runnable, "kafka-consumer-thread-" + i).start();
            }
        }
    }


    public static synchronized void destroy() {
        if(runnables != null) {
            for (KafkaConsumerRunner runnable : runnables) {
                runnable.shutdown();
            }
        }
    }

}
