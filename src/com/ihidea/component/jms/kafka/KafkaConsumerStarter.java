package com.ihidea.component.jms.kafka;

import com.dianping.cat.Cat;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaConsumerStarter {

    private final static Logger logger = LoggerFactory.getLogger(KafkaConsumerStarter.class);

    private static Map<String, Method> consumerMethodMap = new HashMap<String, Method>();

    private synchronized static void initConsumerMethod() throws Exception{

        if(consumerMethodMap.isEmpty()) {

            Map<String, Object> kafkaConsumerClz = SpringContextLoader.getBeansWithAnnotation(KafkaConsumerHandler.class);

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
     * @param consumerThreadNum     consumer实例个数，既consumer线程数，默认为8，建议不要修改。一个consumer对于一个或多个分区，阿里云默认一个topic创建24个分区
     */
    public static void init(String brokerAddress, String consumerGroupName, int sessionTimeOutMs, int maxPollRecords, int consumerThreadNum) throws Exception {

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
                consumerThreadNum = 8;
            }
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
            //循环构造消息对象，即生成consumerThreadNum个消费实例
            for (int i = 0; i < consumerThreadNum; i++) {
                final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
                //设置  Consumer Group 订阅的 Topic，可订阅多个 Topic。如果 GROUP_ID_CONFIG 相同，那建议订阅的 Topic 设置也相同
                consumer.subscribe(consumerMethodMap.keySet());

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //循环消费消息
                            while (true) {
                                ConsumerRecords<String, String> records = consumer.poll(1000);
                                //必须在下次 poll 之前消费完这些数据, 且总耗时不得超过 SESSION_TIMEOUT_MS_CONFIG 的值
                                //建议开一个单独的线程池来消费消息，然后异步返回结果
                                for (ConsumerRecord<String, String> record : records) {
                                    try {
                                        String topicName = record.topic();
                                        Method method = consumerMethodMap.get(topicName);
                                        if (method != null) {
                                            Map<String, Object> paramMap = new HashMap<String, Object>();
                                            paramMap.put("record", record);
                                            ClassUtilsEx.invokeMethod(method.getDeclaringClass().getSimpleName(), method.getName(), paramMap);
                                        } else {
                                            logger.error("[Kafka]处理消息发生异常: topic未找到相应的处理方法" + ",topic=" + topicName);
                                        }
                                    } catch (InvocationTargetException e) {
                                        Throwable targetException = e.getTargetException();
                                        Cat.logError(targetException);
                                        logger.error("[Kafka]处理消息发生异常:" + targetException.getMessage() + ",消息报文:" + JSONUtilsEx.serialize(record), targetException);
                                    } catch (Exception e) {
                                        logger.error("[Kafka]处理消息发生异常:" + e.getMessage() + ",消息报文:" + JSONUtilsEx.serialize(record), e);
                                    }
                                }
                                consumer.commitAsync();
                            }
                        } catch (Exception e) {
                            logger.error("[Kafka]位移提交异常:", e);
                        } finally {
                            try {
                                consumer.commitSync(); // 最后一次提交使用同步阻塞式提交
                            } finally {
                                consumer.close();
                            }
                        }
                    }
                };

                new Thread(runnable, "kafka-consumer-thread-" + i).start();
            }
        }
    }


}
